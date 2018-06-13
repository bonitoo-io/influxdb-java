package org.influxdb.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.influxdb.InfluxDBException;
import org.influxdb.InfluxDBOptions;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveListener;
import org.influxdb.reactive.InfluxDBReactiveListenerDefault;
import org.influxdb.reactive.QueryOptions;
import org.reactivestreams.Publisher;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
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
    private final BatchOptionsReactive batchOptions;
    private final InfluxDBReactiveListener listener;
    private final InfluxDBResultMapper resultMapper;

    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options) {
        this(options, BatchOptionsReactive.DEFAULTS);
    }

    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                                @Nonnull final BatchOptionsReactive batchOptions) {

        this(options, batchOptions, new InfluxDBReactiveListenerDefault());
    }


    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                                @Nonnull final BatchOptionsReactive batchOptions,
                                @Nonnull final InfluxDBReactiveListener listener) {

        this(options, batchOptions, Schedulers.newThread(), Schedulers.computation(), Schedulers.trampoline(),
                Schedulers.trampoline(), null, listener);
    }

    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final BatchOptionsReactive batchOptions,
                         @Nonnull final Scheduler processorScheduler,
                         @Nonnull final Scheduler batchScheduler,
                         @Nonnull final Scheduler jitterScheduler,
                         @Nonnull final Scheduler retryScheduler,
                         @Nullable final InfluxDBServiceReactive influxDBService,
                         @Nonnull final InfluxDBReactiveListener listener) {

        super(InfluxDBServiceReactive.class, options, influxDBService, null);

        this.options = options;
        this.batchOptions = batchOptions;
        this.listener = listener;

        this.resultMapper = new InfluxDBResultMapper();

        this.processor = PublishProcessor.create();
        Disposable writeDisposable = this.processor
                //
                // Backpressure
                //
                .onBackpressureBuffer(
                        batchOptions.getBufferLimit(),
                        listener::doOnBackpressure,
                        batchOptions.getBackpressureStrategy())
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
                .compose(applyJitter(jitterScheduler))
                .doOnError(this.listener::doOnError)
                .subscribe(new WritePointsConsumer(retryScheduler));

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
    public <M> Flowable<M> query(@Nonnull final Query query, @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(query, "Query is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");

        return query(query, measurementType, QueryOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Query query,
                                 @Nonnull final Class<M> measurementType,
                                 @Nonnull final QueryOptions queryOptions) {

        Objects.requireNonNull(query, "Query is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");
        Objects.requireNonNull(queryOptions, "QueryOptions is required");

        return query(Flowable.just(query), measurementType, queryOptions);
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Publisher<Query> query, @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(query, "Query publisher is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");

        return query(query, measurementType, QueryOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Publisher<Query> query,
                                 @Nonnull final Class<M> measurementType,
                                 @Nonnull final QueryOptions queryOptions) {

        Objects.requireNonNull(query, "Query publisher is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");
        Objects.requireNonNull(queryOptions, "QueryOptions is required");

        return query(query, queryOptions)
                .map(queryResult -> resultMapper.toPOJO(queryResult, measurementType))
                .concatMap((Function<List<M>, Flowable<M>>) Flowable::fromIterable);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Query query) {

        Objects.requireNonNull(query, "Query is required");

        return query(query, QueryOptions.DEFAULTS);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Query query, @Nonnull final QueryOptions queryOptions) {

        Objects.requireNonNull(query, "Query is required");
        Objects.requireNonNull(queryOptions, "QueryOptions is required");

        return query(Flowable.just(query), queryOptions);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Publisher<Query> queryStream) {

        Objects.requireNonNull(queryStream, "Query publisher is required");

        return query(queryStream, QueryOptions.DEFAULTS);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Publisher<Query> queryStream,
                                       @Nonnull final QueryOptions queryOptions) {

        Objects.requireNonNull(queryStream, "Query publisher is required");
        Objects.requireNonNull(queryOptions, "QueryOptions is required");

        return Flowable.fromPublisher(queryStream).concatMap((Function<Query, Publisher<QueryResult>>) query -> {

            //
            // Parameters
            //
            String username = options.getUsername();
            String password = options.getPassword();
            String database = query.getDatabase();

            String precision = TimeUtil.toTimePrecision(options.getPrecision());

            int chunkSize = queryOptions.getChunkSize();
            String rawQuery = query.getCommandWithUrlEncoded();

            String params = "";
            if (query instanceof BoundParameterQuery) {
                params = ((BoundParameterQuery) query).getParameterJsonWithUrlEncoded();
            }
            return influxDBService
                    .query(username, password, database, precision, chunkSize, rawQuery, params)
                    .flatMap(
                            // success response
                            this::chunkReader,
                            // error response
                            throwable -> Observable.error(buildInfluxDBException(throwable)),
                            // end of response
                            Observable::empty)
                    .toFlowable(BackpressureStrategy.BUFFER);
        });
    }

    @Override
    public void close() {

        LOG.log(Level.INFO, "Flushing any cached metrics before shutdown.");

        processor.onComplete();
    }

    @Nonnull
    private FlowableTransformer<Flowable<Point>, Flowable<Point>> applyJitter(@Nonnull final Scheduler scheduler) {

        Objects.requireNonNull(scheduler, "Jitter scheduler is required");

        return source -> {

            //
            // source without jitter
            //
            if (batchOptions.getJitterInterval() <= 0) {
                return source;
            }

            //
            // Add jitter => dynamic delay
            //
            return source.delay((Function<Flowable<Point>, Flowable<Long>>) pointFlowable -> {

                int delay = jitterDelay();

                LOG.log(Level.FINEST, "Generated Jitter dynamic delay: {0}", delay);

                return Flowable.timer(delay, TimeUnit.MILLISECONDS, scheduler);
            });
        };
    }

    private final class WritePointsConsumer implements Consumer<Flowable<Point>> {

        private final Scheduler retryScheduler;

        private WritePointsConsumer(@Nonnull final Scheduler retryScheduler) {

            Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

            this.retryScheduler = retryScheduler;
        }

        @Override
        public void accept(final Flowable<Point> flowablePoints) {

            Action success = listener::doOnSuccessResponse;

            Consumer<Throwable> fail = throwable -> {

                if (throwable instanceof HttpException) {

                    InfluxDBException influxDBException = buildInfluxDBException(throwable);

                    listener.doOnErrorResponse(influxDBException);
                } else {

                    listener.doOnError(throwable);
                }
            };

            flowablePoints
                    //
                    // Point => InfluxDB Line Protocol
                    //
                    .map(Point::lineProtocol)
                    .toList()
                    .filter(points -> !points.isEmpty())
                    //
                    // InfluxDB Line Protocol => to Request Body
                    //
                    .map(points -> {

                        String body = points.stream().collect(Collectors.joining("\n"));

                        return RequestBody.create(options.getMediaType(), body);
                    })
                    .subscribe(requestBody -> {

                        //
                        // Parameters
                        //
                        String username = options.getUsername();
                        String password = options.getPassword();
                        String database = options.getDatabase();

                        String retentionPolicy = options.getRetentionPolicy();
                        String precision = TimeUtil.toTimePrecision(options.getPrecision());
                        String consistencyLevel = options.getConsistencyLevel().value();

                        influxDBService.writePoints(
                                username, password, database,
                                retentionPolicy, precision, consistencyLevel,
                                requestBody)
                                //
                                // Retry strategy
                                //
                                .retryWhen(retryCapabilities(retryScheduler))
                                .subscribe(success, fail);
                    });
        }
    }

    /**
     * The retry handler that tries to retry a write if it failed previously and
     * the reason of the failure is not permanent.
     *
     * @param retryScheduler for scheduling retry write
     * @return the retry handler
     */
    @Nonnull
    private Function<Flowable<Throwable>, Publisher<?>> retryCapabilities(@Nonnull final Scheduler retryScheduler) {

        Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

        return errors -> errors.flatMap(throwable -> {

            if (throwable instanceof HttpException) {

                InfluxDBException influxDBException = buildInfluxDBException(throwable);

                //
                // Retry request
                //
                if (influxDBException.isRetryWorth()) {

                    listener.doOnErrorResponse(influxDBException);

                    int retryInterval = batchOptions.getRetryInterval() + jitterDelay();

                    return Flowable.just("notify").delay(retryInterval, TimeUnit.MILLISECONDS, retryScheduler);
                }
            }

            //
            // This type of throwable is not able to retry
            //
            return Flowable.error(throwable);
        });
    }

    @Nonnull
    private InfluxDBException buildInfluxDBException(@Nonnull final Throwable throwable) {

        Objects.requireNonNull(throwable, "Throwable is required");

        if (throwable instanceof HttpException) {

            String errorMessage = ((HttpException) throwable).response().headers().get("X-Influxdb-Error");
            if (errorMessage != null) {
                return InfluxDBException.buildExceptionFromErrorMessage(errorMessage);
            }
        }

        return new InfluxDBException(throwable);
    }

    private int jitterDelay() {

        return (int) (Math.random() * batchOptions.getJitterInterval());
    }

    @Nonnull
    private Observable<QueryResult> chunkReader(@Nonnull final ResponseBody body) {

        Objects.requireNonNull(body, "ResponseBody is required");

        return Observable.create(subscriber -> {

            boolean isCompleted = false;
            try {
                BufferedSource source = body.source();

                //
                // Subscriber is not disposed && source has data => parse
                //
                while (!subscriber.isDisposed() && !source.exhausted()) {

                    QueryResult queryResult = InfluxDBReactiveImpl.this.adapter.fromJson(source);
                    subscriber.onNext(queryResult);
                    listener.doOnQueryResult();
                }
            } catch (IOException e) {

                //
                // Socket close by remote server or end of data
                //
                if (e.getMessage().equals("Socket closed") || e instanceof EOFException) {
                    isCompleted = true;
                    subscriber.onComplete();
                } else {
                    throw new UncheckedIOException(e);
                }
            }

            //if response end we get here
            if (!isCompleted) {
                subscriber.onComplete();
            }

            body.close();
        });
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
