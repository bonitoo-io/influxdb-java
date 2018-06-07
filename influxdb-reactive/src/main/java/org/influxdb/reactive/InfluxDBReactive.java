package org.influxdb.reactive;


import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.annotations.Experimental;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;

/**
 * Proof-of-concept API.
 * <p>
 * Not Implemented:
 * <ul>
 *     <li>Partial writes = writeMeasurements(good,good,wrong,good)...</li>
 *     <li>UDP</li>
 *     <li>Body as Flowable</li>
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
     * Write a bag of Measurements to the default database.
     *
     * @param measurements The measurements to write
     * @param <M>          The type of the measurement (POJO)
     * @return {@link Flowable} emitting the saved measurements.
     */
    <M> Flowable<M> writeMeasurements(@Nonnull final Iterable<M> measurements);

    /**
     * Write a stream of Measurements to the default database.
     *
     * @param measurementStream The stream of measurements to write
     * @param <M>               The type of the measurement (POJO)
     * @return {@link Flowable} emitting the saved measurements.
     */
    <M> Flowable<M> writeMeasurements(@Nonnull final Publisher<M> measurementStream);

    /**
     * Write a single Point to the default database.
     *
     * @param point The point to write
     * @return {@link Maybe} emitting the saved point.
     */
    Maybe<Point> writePoint(@Nonnull final Point point);

    /**
     * Write a bag of Points to the default database.
     *
     * @param points The points to write
     * @return {@link Flowable} emitting the saved points.
     */
    Flowable<Point> writePoints(@Nonnull final Iterable<Point> points);

    /**
     * Write a stream of Points to the default database.
     *
     * @param pointStream The stream of points to write
     * @return {@link Flowable} emitting the saved points.
     */
    Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream);

    /**
     * Execute a query against a default database.
     *
     * @param query the query to execute.
     * @return {@link Maybe} emitting a List of Series which matched the query or {@link Maybe#empty()} if none found.
     */
    Maybe<QueryResult> query(@Nonnull final Query query);

    /**
     * Execute a query against a default database.
     *
     * @param query the query to execute. Uses the first emitted element to perform the find-query.
     * @return {@link Maybe} emitting a List of Series which matched the query or {@link Maybe#empty()} if none found.
     */
    Maybe<QueryResult> query(@Nonnull final Publisher<Query> query);

    /**
     * Execute a query against a default database.
     *
     * @param query           the query to execute.
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a List of Series which matched the query or {@link Flowable#empty()} if none
     * found.
     */
    <M> Flowable<M> query(@Nonnull final Query query, @Nonnull final Class<M> measurementType);

    /**
     * Execute a query against a default database.
     *
     * @param query           the query to execute. Uses the first emitted element to perform the find-query.
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a List of Series which matched the query or {@link Maybe#empty()} if none
     * found.
     */
    <M> Flowable<M> query(@Nonnull final Publisher<Query> query, @Nonnull final Class<M> measurementType);

    /**
     * Close thread for asynchronous batch writes.
     */
    void close();
}
