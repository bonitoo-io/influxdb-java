package org.influxdb.reactive.event;

import org.influxdb.dto.Point;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the success response from InfluxDB server.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 09:14)
 * @since 3.0.0
 */
public class WriteSuccessEvent extends AbstractInfluxEvent {

    private static final Logger LOG = Logger.getLogger(WriteSuccessEvent.class.getName());

    private final List<Point> points;

    public WriteSuccessEvent(@Nonnull final List<Point> points) {

        Objects.requireNonNull(points, "Points are required");

        this.points = points;
    }

    /**
     * @return the successful wrote points
     */
    @Nonnull
    public List<Point> getPoints() {
        return points;
    }

    @Override
    protected void logEvent() {
        LOG.log(Level.FINEST, "Success response from InfluxDB");
    }
}
