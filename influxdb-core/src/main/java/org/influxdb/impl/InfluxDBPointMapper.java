package org.influxdb.impl;

import org.influxdb.InfluxDBMapperException;
import org.influxdb.annotation.Column;
import org.influxdb.dto.Point;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class responsible for mapping a POJO to a {@link org.influxdb.dto.Point}.
 *
 * @author Jakub Bednar (bednar@github) (21/06/2018 08:05)
 */
@ThreadSafe
public class InfluxDBPointMapper extends AbstractInfluxDBMapper {

    private static final Logger LOG = Logger.getLogger(InfluxDBPointMapper.class.getName());

    /**
     * Map the {@code measurement} to {@link Point}.
     *
     * @param measurement for mapping to {@link Point}
     * @param precision   the precision to use for store {@code time} of {@link Point}
     * @param <M>         type of measurement
     * @return a {@link Point} created from {@code measurement}
     * @throws InfluxDBMapperException if the {@code measurement} can't be mapped to {@link Point}
     */
    @Nonnull
    public <M> Point toPoint(@Nonnull final M measurement, @Nonnull final TimeUnit precision)
            throws InfluxDBMapperException {

        Objects.requireNonNull(measurement, "Measurement is required");
        Objects.requireNonNull(precision, "TimeUnit precision is required");

        Class<?> measurementType = measurement.getClass();
        cacheMeasurementClass(measurementType);

        Point.Builder builder = Point.measurement(getMeasurementName(measurementType));

        CLASS_FIELD_CACHE.get(measurementType.getName()).forEach((name, field) -> {

            Column column = field.getAnnotation(Column.class);

            Object value;
            try {
                field.setAccessible(true);
                value = field.get(measurement);
            } catch (IllegalAccessException e) {

                String msg = String.format("Field '%s' of '%s' is not accessible", field.getName(), measurement);

                throw new InfluxDBMapperException(msg, e);
            }

            if (value == null) {
                LOG.log(Level.FINEST, "Field {0} of {1} has null value", new Object[]{field.getName(), measurement});
                return;
            }

            Class<?> fieldType = field.getType();
            if (column.tag()) {
                builder.tag(column.name(), value.toString());
            } else if (isNumber(fieldType)) {
                builder.addField(column.name(), (Number) value);
            } else if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
                builder.addField(column.name(), (Boolean) value);
            } else if (String.class.isAssignableFrom(fieldType)) {
                builder.addField(column.name(), (String) value);
            } else if (Instant.class.isAssignableFrom(fieldType)) {
                Instant instant = (Instant) value;
                long timeToSet = precision.convert(instant.toEpochMilli(), TimeUnit.MILLISECONDS);
                builder.time(timeToSet, precision);
            }
        });

        Point point = builder.build();
        LOG.log(Level.FINEST, "Mapped measurement: {0} to Point: {1}", new Object[]{measurement, point});

        return point;
    }

    private boolean isNumber(@Nonnull final Class<?> fieldType) {
        return Number.class.isAssignableFrom(fieldType)
                || double.class.isAssignableFrom(fieldType)
                || long.class.isAssignableFrom(fieldType)
                || int.class.isAssignableFrom(fieldType);
    }
}
