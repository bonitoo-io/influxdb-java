package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBServiceReactive;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:37)
 */
class InfluxDBReactiveImpl implements InfluxDBReactive {

    private static final okhttp3.MediaType MEDIA_TYPE_STRING = MediaType.parse("text/plain");

    private final InfluxDBOptions options;
    private final InfluxDBServiceReactive influxDBService;
    private final PublishProcessor<Point> processor;

    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final InfluxDBServiceReactive influxDBService) {

        Objects.requireNonNull(options, "InfluxDBOptions is required");
        Objects.requireNonNull(influxDBService, "InfluxDBServiceReactive is required");

        this.options = options;
        this.influxDBService = influxDBService;

        this.processor = PublishProcessor.create();
        this.processor.buffer(1).subscribe(new WritePointsConsumer());
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
    public Maybe<Point> writePoint(@Nonnull final Point point) {

        return writePoints(Flowable.just(point)).firstElement();
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Iterable<Point> points) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream) {

        Flowable<Point> emmitting = Flowable.fromPublisher(pointStream);

        emmitting.subscribe(processor::onNext);

        return emmitting;
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

    private class WritePointsConsumer implements Consumer<List<Point>> {

        @Override
        public void accept(final List<Point> points) {

            String lineProtocols = points.stream()
                    .map(Point::lineProtocol)
                    .collect(Collectors.joining("\\n"));

            RequestBody body = RequestBody.create(MEDIA_TYPE_STRING, lineProtocols);

            String username = options.getUsername();
            String password = options.getPassword();
            String database = options.getDatabase();
            InfluxDB.ConsistencyLevel consistencyLevel = options.getConsistencyLevel();
            String retentionPolicy = options.getRetentionPolicy();

            influxDBService.writePointsReactive(
                    username, password, database,
                    retentionPolicy, "", consistencyLevel.value(),
                    body);
        }
    }
}
