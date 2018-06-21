package org.influxdb.impl;

import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (18/06/2018 14:57)
 */
final class MeasurementData<M> extends AbstractData<M> {

    private static final Logger LOG = Logger.getLogger(MeasurementData.class.getName());

    private M measurement;

    MeasurementData(@Nonnull final M measurement, @Nonnull final WriteOptions writeOptions) {
        super(writeOptions);
        this.measurement = measurement;
    }

    @Nonnull
    @Override
    M getData() {
        return measurement;
    }

    @Nonnull
    @Override
    String lineProtocol() {

        String lineProtocol = new InfluxDBPointMapper()
                .toPoint(measurement, writeOptions.getPrecision())
                .lineProtocol(writeOptions.getPrecision());

        Object[] params = {measurement, lineProtocol};
        LOG.log(Level.FINEST, "Map measurement: {0} to InfluxDB Line Protocol: {1}", params);

        return lineProtocol;
    }
}
