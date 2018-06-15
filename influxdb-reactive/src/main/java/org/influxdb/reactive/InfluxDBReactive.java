package org.influxdb.reactive;


import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.Experimental;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.reactive.event.AbstractInfluxEvent;
import org.influxdb.reactive.option.QueryOptions;
import org.influxdb.reactive.option.WriteOptions;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;

/**
 * Proof-of-concept API.
 * <p>
 * Not Implemented:
 * <ul>
 * <li>Partial writes = writeMeasurements(good,good,wrong,good)... -
 * https://docs.influxdata.com/influxdb/v1.5/guides/writing_data/#writing-multiple-points</li>
 * <li>UDP</li>
 * <li>Body as Flowable</li>
 * <li>Use flat in write</li>
 * <li>Retry for wrong url</li>
 * <li>Write line protocol</li>
 * </ul>
 *
 * @author Jakub Bednar (bednar@github) (29/05/2018 14:58)
 * @since 3.0.0
 */
@Experimental
public interface InfluxDBReactive {

    /**
     * Write a single Measurement to the default database.
     *
     * @param measurement The measurement to write
     * @param <M>         The type of the measurement (POJO)
     * @return {@link Maybe} emitting the saved measurement.
     */
    <M> Maybe<M> writeMeasurement(@Nonnull final M measurement);

    /**
     * Write a single Measurement to the default database.
     *
     * @param measurement The measurement to write
     * @param <M>         The type of the measurement (POJO)
     * @param options     the configuration of the write
     * @return {@link Maybe} emitting the saved measurement.
     */
    <M> Maybe<M> writeMeasurement(@Nonnull final M measurement, @Nonnull final WriteOptions options);

    /**
     * Write a bag of Measurements to the default database.
     *
     * @param measurements The measurements to write
     * @param <M>          The type of the measurement (POJO)
     * @return {@link Flowable} emitting the saved measurements.
     */
    <M> Flowable<M> writeMeasurements(@Nonnull final Iterable<M> measurements);

    /**
     * Write a bag of Measurements to the default database.
     *
     * @param measurements The measurements to write
     * @param <M>          The type of the measurement (POJO)
     * @param options      the configuration of the write
     * @return {@link Flowable} emitting the saved measurements.
     */
    <M> Flowable<M> writeMeasurements(@Nonnull final Iterable<M> measurements, @Nonnull final WriteOptions options);

    /**
     * Write a stream of Measurements to the default database.
     *
     * @param measurementStream The stream of measurements to write
     * @param <M>               The type of the measurement (POJO)
     * @return {@link Flowable} emitting the saved measurements.
     */
    <M> Flowable<M> writeMeasurements(@Nonnull final Publisher<M> measurementStream);

    /**
     * Write a stream of Measurements to the default database.
     *
     * @param measurementStream The stream of measurements to write
     * @param <M>               The type of the measurement (POJO)
     * @param options           the configuration of the write
     * @return {@link Flowable} emitting the saved measurements.
     */
    <M> Flowable<M> writeMeasurements(@Nonnull final Publisher<M> measurementStream,
                                      @Nonnull final WriteOptions options);

    /**
     * Write a single Point to the default database.
     *
     * @param point The point to write
     * @return {@link Maybe} emitting the saved point.
     */
    Maybe<Point> writePoint(@Nonnull final Point point);

    /**
     * Write a single Point to the default database.
     *
     * @param point   The point to write
     * @param options the configuration of the write
     * @return {@link Maybe} emitting the saved point.
     */
    Maybe<Point> writePoint(@Nonnull final Point point, @Nonnull final WriteOptions options);

    /**
     * Write a bag of Points to the default database.
     *
     * @param points The points to write
     * @return {@link Flowable} emitting the saved points.
     */
    Flowable<Point> writePoints(@Nonnull final Iterable<Point> points);

    /**
     * Write a bag of Points to the default database.
     *
     * @param points  The points to write
     * @param options the configuration of the write
     * @return {@link Flowable} emitting the saved points.
     */
    Flowable<Point> writePoints(@Nonnull final Iterable<Point> points, @Nonnull final WriteOptions options);

    /**
     * Write a stream of Points to the default database.
     *
     * @param pointStream The stream of points to write
     * @return {@link Flowable} emitting the saved points.
     */
    Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream);

    /**
     * Write a stream of Points to the default database.
     *
     * @param pointStream The stream of points to write
     * @param options     the configuration of the write
     * @return {@link Flowable} emitting the saved points.
     */
    Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream, @Nonnull final WriteOptions options);

    /**
     * Execute a query against a default database.
     *
     * @param query           the query to execute.
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a List of Series mapped to {@code measurementType} which are matched the query
     * or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> query(@Nonnull final Query query, @Nonnull final Class<M> measurementType);

    /**
     * Execute a query against a default database.
     *
     * @param query           the query to execute.
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @param queryOptions    the configuration of the query
     * @return {@link Flowable} emitting a List of Series mapped to {@code measurementType} which are matched the query
     * or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> query(@Nonnull final Query query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final QueryOptions queryOptions);

    /**
     * Execute a query against a default database.
     *
     * @param query           the query to execute. Uses the first emitted element to perform the find-query.
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a List of Series mapped to {@code measurementType} which are matched the query
     * or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> query(@Nonnull final Publisher<Query> query, @Nonnull final Class<M> measurementType);

    /**
     * Execute a query against a default database.
     *
     * @param query           the query to execute. Uses the first emitted element to perform the find-query.
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @param queryOptions    the configuration of the query
     * @return {@link Flowable} emitting a List of Series mapped to {@code measurementType} which are matched the query
     * or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> query(@Nonnull final Publisher<Query> query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final QueryOptions queryOptions);

    /**
     * Execute a query against a default database.
     *
     * @param query the query to execute.
     * @return {@link Single} emitting a List of Series which matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<QueryResult> query(@Nonnull final Query query);

    /**
     * Execute a query against a default database.
     *
     * @param query   the query to execute.
     * @param options the configuration of the query
     * @return {@link Single} emitting a List of Series which matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<QueryResult> query(@Nonnull final Query query, @Nonnull final QueryOptions options);

    /**
     * Execute a query against a default database.
     *
     * @param queryStream the query to execute. Uses the first emitted element to perform the find-query.
     * @return {@link Single} emitting a List of Series which matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<QueryResult> query(@Nonnull final Publisher<Query> queryStream);

    /**
     * Execute a query against a default database.
     *
     * @param queryStream the query to execute. Uses the first emitted element to perform the find-query.
     * @param options     the configuration of the query
     * @return {@link Single} emitting a List of Series which matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<QueryResult> query(@Nonnull final Publisher<Query> queryStream, @Nonnull final QueryOptions options);

    /**
     * Listen the events produced by {@link InfluxDBReactive}.
     *
     * @param eventType type of event to listen
     * @param <T>       type of event to listen
     * @return lister for {@code eventType} events
     */
    @Nonnull
    <T extends AbstractInfluxEvent> Observable<T> listenEvents(@Nonnull Class<T> eventType);

    /**
     * Ping this he connected InfluxDB Server.
     *
     * @return the response of the ping execution.
     */
    Maybe<Pong> ping();

    /**
     * Return the version of the connected InfluxDB Server.
     *
     * @return the version String, otherwise unknown.
     */
    @Nonnull
    Maybe<String> version();

    /**
     * Enable Gzip compress for http request body.
     *
     * @return the InfluxDBReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBReactive enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the InfluxDBReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBReactive disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * Close thread for asynchronous batch writes.
     */
    @Nonnull
    InfluxDBReactive close();

    /**
     * @return {@link Boolean#TRUE} if all metrics are flushed
     */
    boolean isClosed();
}
