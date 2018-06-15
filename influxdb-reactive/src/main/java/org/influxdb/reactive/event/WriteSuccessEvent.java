package org.influxdb.reactive.event;

import org.influxdb.dto.Point;
import org.influxdb.reactive.option.WriteOptions;

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
    private final WriteOptions writeOptions;

    public WriteSuccessEvent(@Nonnull final List<Point> points, @Nonnull final WriteOptions writeOptions) {

        Objects.requireNonNull(points, "Points are required");
        Objects.requireNonNull(writeOptions, "WriteOptions are required");

        this.points = points;
        this.writeOptions = writeOptions;
    }

    /**
     * @return the successful wrote points
     */
    @Nonnull
    public List<Point> getPoints() {
        return points;
    }

    /**
     * @return {@code writeOptions} that was used in write
     */
    @Nonnull
    public WriteOptions getWriteOptions() {
        return writeOptions;
    }

    @Override
    protected void logEvent() {
        LOG.log(Level.FINEST, "Success response from InfluxDB");
    }
}
