package org.influxdb.reactive.event;

import org.influxdb.InfluxDBException;
import org.influxdb.dto.Point;
import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the partial error response from InfluxDB server.
 *
 * @author Jakub Bednar (bednar@github) (15/06/2018 12:49) zaz
 */
public class WritePartialEvent extends AbstractInfluxEvent {

    private static final Logger LOG = Logger.getLogger(WritePartialEvent.class.getName());

    private final List<Point> points;
    private final WriteOptions writeOptions;
    private final InfluxDBException.PartialWriteException exception;

    public WritePartialEvent(@Nonnull final List<Point> points,
                             @Nonnull final WriteOptions writeOptions,
                             @Nonnull final InfluxDBException.PartialWriteException exception) {

        Objects.requireNonNull(points, "Points are required");
        Objects.requireNonNull(writeOptions, "WriteOptions are required");
        Objects.requireNonNull(exception, "InfluxDBException is required");

        this.points = points;
        this.writeOptions = writeOptions;
        this.exception = exception;
    }

    /**
     * @return the points that was sent to InfluxDB
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

    /**
     * @return the partial exception that was throw
     */
    @Nonnull
    public InfluxDBException.PartialWriteException getException() {
        return exception;
    }

    @Override
    protected void logEvent() {
        LOG.log(Level.FINEST, "Success response from InfluxDB");
    }
}
