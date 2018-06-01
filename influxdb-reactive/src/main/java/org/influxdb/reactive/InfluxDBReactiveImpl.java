package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBServiceReactive;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:37)
 */
class InfluxDBReactiveImpl implements InfluxDBReactive {

    private final InfluxDBOptions options;
    private final InfluxDBServiceReactive influxDBService;

    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final InfluxDBServiceReactive influxDBService) {

        Objects.requireNonNull(options, "InfluxDBOptions is required");
        Objects.requireNonNull(influxDBService, "InfluxDBServiceReactive is required");

        this.options = options;
        this.influxDBService = influxDBService;
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

//        private final PublishProcessor<Point> processor = PublishProcessor.create();
//        influxDBService.
//                writePointsReactive(
//                        options.getUsername(),
//                        options.getPassword(),
//                        options.getDatabase(),
//                        options.getRetentionPolicy(),
//                        "",
//                        options.getConsistencyLevel().value(),
//                        null);

//        processor.window(2).subscribe(onNext ->
//                {
//                    onNext.map(Point::lineProtocol).toList().subscribe(points -> {
//
//                        String collect = points.stream().collect(Collectors.joining("\\n"));
//
//                        System.out.println("collect = " + collect);
//                    });
//                },
//                this::logError,
//                this::logEnd);
//
//        publish = processor.publish();

        return Flowable.fromPublisher(pointStream);
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
