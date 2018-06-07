package org.influxdb.impl;

import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import org.influxdb.InfluxDBException;
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
import retrofit2.HttpException;
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

    private static final Logger LOG = Logger.getLogger(InfluxDBReactiveImpl.class.getName());

    private final PublishProcessor<Point> processor;

    private final InfluxDBOptions options;
    private final InfluxDBReactiveListener listener;

    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options) {
        this(options, BatchOptionsReactive.DEFAULTS);
    }

    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                                @Nonnull final BatchOptionsReactive defaults) {

        this(options, defaults, new InfluxDBReactiveListenerDefault());
    }


    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final BatchOptionsReactive batchOptions,
                         @Nonnull final InfluxDBReactiveListener listener) {
        this(options, batchOptions,
                Schedulers.newThread(),
                Schedulers.computation(),
                Schedulers.trampoline(),
                null,
                listener);
    }

    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final BatchOptionsReactive batchOptions,
                         @Nonnull final Scheduler processorScheduler,
                         @Nonnull final Scheduler batchScheduler,
                         @Nonnull final Scheduler jitterScheduler,
                         @Nullable final InfluxDBServiceReactive influxDBService,
                         @Nonnull final InfluxDBReactiveListener listener) {

        super(InfluxDBServiceReactive.class, options, influxDBService, null);

        this.options = options;
        this.listener = listener;

        this.processor = PublishProcessor.create();
        Disposable writeDisposable = this.processor
                //
                // Backpressure
                //
                .onBackpressureBuffer(
                        batchOptions.getBufferLimit(),
                        listener::doOnBackpressure,
                        BackpressureOverflowStrategy.DROP_OLDEST)
                .observeOn(processorScheduler)
                //
                // Batching
                //
                .window(batchOptions.getFlushInterval(),
                        TimeUnit.MILLISECONDS,
                        batchScheduler,
                        batchOptions.getActions(),
                        true)
                //
                // Jitter interval
                //
                .compose(applyJitter(jitterScheduler, batchOptions.getJitterInterval()))
                .doOnError(this.listener::doOnError)
                .subscribe(new WritePointsConsumer());

        this.listener.doOnSubscribeWriter(writeDisposable);
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

    @Nonnull
    private FlowableTransformer<Flowable<Point>, Flowable<Point>> applyJitter(@Nonnull final Scheduler jitterScheduler,
                                                                              @Nonnull final Integer jitterInterval) {

        Objects.requireNonNull(jitterScheduler, "Jitter scheduler is required");
        Objects.requireNonNull(jitterInterval, "Jitter interval is required");

        return source -> {

            //
            // source without jitter
            //
            if (jitterInterval <= 0) {
                return source;
            }

            //
            // Add jitter => dynamic delay
            //
            return source.delay((Function<Flowable<Point>, Flowable<Long>>) pointFlowable -> {

                int delay = (int) (Math.random() * jitterInterval);

                LOG.log(Level.FINEST, "Generated Jitter dynamic delay: {0}", delay);

                return Flowable.timer(delay, TimeUnit.MILLISECONDS, jitterScheduler);
            });
        };
    }

    private class WritePointsConsumer implements Consumer<Flowable<Point>> {

        @Override
        public void accept(final Flowable<Point> flowablePoints) {

            flowablePoints
                    .map(Point::lineProtocol)
                    .toList()
                    .filter(points -> !points.isEmpty())
                    .map(points -> {

                        String body = points.stream().collect(Collectors.joining("\n"));

                        return RequestBody.create(options.getMediaType(), body);
                    })
                    .subscribe(requestBody -> {

                        Action success = listener::doOnSuccessResponse;

                        Consumer<Throwable> fail = throwable -> {

                            if (throwable instanceof HttpException) {

                                InfluxDBException influxDBException =
                                        buildInfluxDBException((HttpException) throwable);

                                listener.doOnErrorResponse(influxDBException);
                                return;
                            }

                            listener.doOnError(throwable);
                        };

                        //
                        // Parameters
                        //
                        String username = options.getUsername();
                        String password = options.getPassword();
                        String database = options.getDatabase();

                        String retentionPolicy = options.getRetentionPolicy();
                        String precision = TimeUtil.toTimePrecision(options.getPrecision());
                        String consistencyLevel = options.getConsistencyLevel().value();

                        influxDBService
                                .writePoints(
                                        username, password, database,
                                        retentionPolicy, precision, consistencyLevel,
                                        requestBody)
                                .retryWhen(retryCapabilities())
                                .subscribe(success, fail);
                    });
        }
    }

    @Nonnull
    private Function<Flowable<Throwable>, Publisher<?>> retryCapabilities() {

        return errors -> errors.flatMap(throwable -> {

            if (throwable instanceof HttpException) {

                InfluxDBException influxDBException =
                        buildInfluxDBException((HttpException) throwable);

                boolean retryWorth = influxDBException.isRetryWorth();
                // TODO retry
            }

            // only notify
            return Flowable.error(throwable);
        });
    }

    @Nonnull
    private InfluxDBException buildInfluxDBException(@Nonnull final HttpException throwable) {
        Objects.requireNonNull(throwable, "Throwable is required");

        Response<?> response = ((HttpException) throwable).response();

        String errorMessage = response.headers().get("X-Influxdb-Error");
        if (errorMessage != null) {
            return InfluxDBException.buildExceptionFromErrorMessage(errorMessage);
        }

        return new InfluxDBException(throwable);
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
