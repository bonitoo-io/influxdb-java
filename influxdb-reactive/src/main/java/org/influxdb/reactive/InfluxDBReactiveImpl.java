package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.influxdb.InfluxDBOptions;
import org.influxdb.impl.InfluxDBServiceReactive;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:37)
 */
class InfluxDBReactiveImpl implements InfluxDBReactive {

    private final InfluxDBServiceReactive influxDBService;

    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final InfluxDBServiceReactive influxDBService) {

        Objects.requireNonNull(options, "InfluxDBOptions is required");
        Objects.requireNonNull(influxDBService, "InfluxDBServiceReactive is required");

        this.influxDBService = influxDBService;
    }

    @Override
    public Maybe<Point> writePoint(@Nonnull final Point point) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Iterable<Point> points) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public <M> Maybe<M> writeMeasurement(@Nonnull final M measurement) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Iterable<M> measurements) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Publisher<M> pointStream) {
        throw new IllegalStateException("Not implemented");
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
}
