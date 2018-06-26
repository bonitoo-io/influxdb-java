package org.influxdb.reactive.events;

import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (18/06/2018 13:38)
 * @since 3.0.0
 */
public abstract class AbstractWriteEvent extends AbstractInfluxEvent {

    private final List<?> points;
    private final WriteOptions writeOptions;

    AbstractWriteEvent(@Nonnull final List<?> points,
                       @Nonnull final WriteOptions writeOptions) {

        Objects.requireNonNull(points, "Points are required");
        Objects.requireNonNull(writeOptions, "WriteOptions are required");

        this.points = points;
        this.writeOptions = writeOptions;
    }

    /**
     * @return the points that was sent to InfluxDB
     */
    @Nonnull
    public <D> List<D> getDataPoints() {

        //noinspection unchecked
        return List.class.cast(points);
    }

    /**
     * @return {@code writeOptions} that was used in write
     */
    @Nonnull
    public WriteOptions getWriteOptions() {
        return writeOptions;
    }
}
