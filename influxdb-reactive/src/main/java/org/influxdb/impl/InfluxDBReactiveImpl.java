package org.influxdb.impl;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import okhttp3.RequestBody;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBOptions;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveListener;
import org.reactivestreams.Publisher;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:37)
 */
public class InfluxDBReactiveImpl extends AbstractInfluxDB<InfluxDBServiceReactive> implements InfluxDBReactive {

    private final InfluxDBOptions options;
    private final PublishProcessor<Point> processor;
    private final InfluxDBReactiveListener listener;

    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options) {
        this(options, BatchOptionsReactive.DEFAULTS);
    }

    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                                @Nonnull final BatchOptionsReactive defaults) {

        this(options, defaults, null, throwable -> {
        });
    }

    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final BatchOptionsReactive batchOptions,
                         @Nullable final InfluxDBServiceReactive influxDBService,
                         @Nonnull final InfluxDBReactiveListener listener) {

        super(InfluxDBServiceReactive.class, options, influxDBService, null);

        this.options = options;

        this.processor = PublishProcessor.create();
        this.listener = listener;

        Scheduler scheduler = batchOptions.getBatchingScheduler();

        //
        // Batching
        //
        Flowable<Flowable<Point>> window = this.processor.window(
                batchOptions.getFlushInterval(),
                TimeUnit.MILLISECONDS,
                scheduler,
                batchOptions.getActions(),
                true);

        //
        // Jitter interval
        //
        if (batchOptions.getJitterInterval() != 0) {
            window = window.timeout((Function<Flowable<Point>, Flowable<Long>>) it -> {

                int timeout = (int) (Math.random() * batchOptions.getJitterInterval());

                return Flowable.timer(timeout, TimeUnit.MILLISECONDS, scheduler);
            }, Flowable.empty());
        }

        window
                .doOnError(this.listener::doOnError)
                .subscribe(new WritePointsConsumer());
    }

    @Override
    protected void configure(@Nonnull final Retrofit.Builder builder) {
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    @Override
    public <M> Maybe<M> writeMeasurement(@Nonnull final M measurement) {

        Objects.requireNonNull(measurement, "Measurement is required");

        return writeMeasurements(Flowable.just(measurement)).firstElement();
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Iterable<M> measurements) {

        Objects.requireNonNull(measurements, "Measurements are required");

        return writeMeasurements(Flowable.fromIterable(measurements));
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Publisher<M> measurementStream) {

        Objects.requireNonNull(measurementStream, "Measurement stream is required");

        Flowable<M> emitting = Flowable.fromPublisher(measurementStream);
        writePoints(emitting.map(new MeasurementToPoint<>()));

        return emitting;
    }

    @Override
    public Maybe<Point> writePoint(@Nonnull final Point point) {

        Objects.requireNonNull(point, "Point is required");

        return writePoints(Flowable.just(point)).firstElement();
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Iterable<Point> points) {

        Objects.requireNonNull(points, "Points are required");

        return writePoints(Flowable.fromIterable(points));
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream) {

        Objects.requireNonNull(pointStream, "Point stream is required");

        Flowable<Point> emitting = Flowable.fromPublisher(pointStream);
        emitting.subscribe(processor::onNext);

        return emitting;
    }

    @Override
    public Maybe<QueryResult> query(@Nonnull final Query query) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Maybe<QueryResult> query(@Nonnull final Publisher<Query> query) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Query query, @Nonnull final Class<M> measurementType) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Publisher<Query> query, @Nonnull final Class<M> measurementType) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void close() {
        processor.onComplete();
    }

    private class WritePointsConsumer implements Consumer<Flowable<Point>> {

        @Override
        public void accept(final Flowable<Point> flowablePoints) {

            flowablePoints
                    .map(Point::lineProtocol)
                    .toList()
                    .filter(points -> !points.isEmpty())
                    .map(points -> {

                        String body = points.stream().collect(Collectors.joining("\\n"));

                        return RequestBody.create(options.getMediaType(), body);
                    })
                    .subscribe(requestBody -> {

                        String username = options.getUsername();
                        String password = options.getPassword();
                        String database = options.getDatabase();
                        InfluxDB.ConsistencyLevel consistencyLevel = options.getConsistencyLevel();
                        String retentionPolicy = options.getRetentionPolicy();

                        Single<Response<String>> responseSingle = influxDBService.writePoints(
                                username, password, database,
                                retentionPolicy,
                                TimeUtil.toTimePrecision(options.getPrecision()),
                                consistencyLevel.value(),
                                //TODO body as flowable points
                                requestBody);

                        //TODO notify success, fail
                        responseSingle.subscribe();
                    });
        }
    }

    /**
     * Just a proof of concept.
     * <p>
     * TODO caching, testing, ...
     */
    private static class MeasurementToPoint<M> implements Function<M, Point> {

        private static final Logger LOG = Logger.getLogger(MeasurementToPoint.class.getName());

        @Override
        public Point apply(final M measurement) {

            Measurement def = measurement.getClass().getAnnotation(Measurement.class);

            Point.Builder point = Point.measurement(def.name());

            for (Field field : measurement.getClass().getDeclaredFields()) {

                Column column = field.getAnnotation(Column.class);
                if (column != null) {

                    String name = column.name();
                    Object value = null;

                    try {
                        field.setAccessible(true);
                        value = field.get(measurement);
                    } catch (IllegalAccessException e) {

                        LOG.log(Level.WARNING,
                                "Field {0} of {1} is not accessible",
                                new Object[]{field.getName(), measurement});
                    }

                    if (value == null) {
                        LOG.log(Level.FINEST, "Field {0} of {1} has null value",
                                new Object[]{field.getName(), measurement});

                        continue;
                    }

                    if (column.tag()) {
                        point.tag(name, value.toString());
                    } else if (Instant.class.isAssignableFrom(field.getType())) {

                        point.time(((Instant) value).toEpochMilli(), def.timeUnit());
                    } else {

                        if (Double.class.isAssignableFrom(value.getClass())) {
                            point.addField(name, (Double) value);
                        } else if (Number.class.isAssignableFrom(value.getClass())) {

                            point.addField(name, (Number) value);
                        } else if (String.class.isAssignableFrom(value.getClass())) {

                            point.addField(name, (String) value);
                        }
                    }
                }
            }

            return point.build();
        }
    }
}
