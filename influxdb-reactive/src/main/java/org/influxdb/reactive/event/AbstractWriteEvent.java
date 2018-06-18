package org.influxdb.reactive.event;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (18/06/2018 13:38)
 * @since 3.0.0
 */
public abstract class AbstractWriteEvent extends AbstractInfluxEvent {

    private final List<?> points;

    AbstractWriteEvent(@Nonnull final List<?> points) {

        Objects.requireNonNull(points, "Points are required");

        this.points = points;
    }

    /**
     * @return the points that was sent to InfluxDB
     */
    @Nonnull
    public <D> List<D> getDataPoints() {

        //noinspection unchecked
        return List.class.cast(points);
    }
}
