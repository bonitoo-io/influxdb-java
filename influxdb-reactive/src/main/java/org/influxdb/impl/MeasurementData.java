package org.influxdb.impl;

import io.reactivex.functions.Function;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Point;
import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.time.Instant;
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

        String lineProtocol = new MeasurementToPoint<>().apply(measurement).lineProtocol();

        Object[] params = {measurement, lineProtocol};
        LOG.log(Level.FINEST, "Map measurement: {0} to InfluxDB Line Protocol: {1}", params);

        return lineProtocol;
    }

    /**
     * Just a proof of concept.
     * <p>
     * TODO caching, testing, ...
     */
    private static class MeasurementToPoint<M> implements Function<M, Point> {

        private static final Logger LOG = Logger.getLogger(MeasurementToPoint.class.getName());

        @Override
        public Point apply(final M measurement) {

            Measurement def = measurement.getClass().getAnnotation(Measurement.class);

            Point.Builder point = Point.measurement(def.name());

            for (Field field : measurement.getClass().getDeclaredFields()) {

                Column column = field.getAnnotation(Column.class);
                if (column != null) {

                    String name = column.name();
                    Object value = null;

                    try {
                        field.setAccessible(true);
                        value = field.get(measurement);
                    } catch (IllegalAccessException e) {

                        Object[] params = {field.getName(), measurement};
                        LOG.log(Level.WARNING, "Field {0} of {1} is not accessible", params);
                    }

                    if (value == null) {
                        Object[] params = {field.getName(), measurement};
                        LOG.log(Level.FINEST, "Field {0} of {1} has null value", params);

                        continue;
                    }

                    if (column.tag()) {
                        point.tag(name, value.toString());
                    } else if (Instant.class.isAssignableFrom(field.getType())) {

                        point.time(((Instant) value).toEpochMilli(), def.timeUnit());
                    } else {

                        if (Double.class.isAssignableFrom(value.getClass())) {
                            point.addField(name, (Double) value);
                        } else if (Number.class.isAssignableFrom(value.getClass())) {

                            point.addField(name, (Number) value);
                        } else if (String.class.isAssignableFrom(value.getClass())) {

                            point.addField(name, (String) value);
                        }
                    }
                }
            }

            return point.build();
        }
    }
}
