package org.influxdb.reactive.event;

import org.influxdb.InfluxDBException;
import org.influxdb.dto.Point;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the error response from InfluxDB server.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 10:16)
 */
public class WriteErrorEvent extends AbstractInfluxEvent {

    private static final Logger LOG = Logger.getLogger(WriteErrorEvent.class.getName());

    private final List<Point> points;
    private final InfluxDBException exception;

    public WriteErrorEvent(@Nonnull final List<Point> points, @Nonnull final InfluxDBException exception) {

        Objects.requireNonNull(points, "Points are required");
        Objects.requireNonNull(exception, "InfluxDBException is required");

        this.points = points;
        this.exception = exception;
    }

    /**
     * @return the unsuccessful wrote points
     */
    @Nonnull
    public List<Point> getPoints() {
        return points;
    }

    /**
     * @return the exception that was throw
     */
    @Nonnull
    public InfluxDBException getException() {
        return exception;
    }

    @Override
    protected void logEvent() {

        LOG.log(Level.SEVERE, "Error response from InfluxDB: ", exception);
    }
}
