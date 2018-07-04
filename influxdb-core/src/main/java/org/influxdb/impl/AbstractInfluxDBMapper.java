package org.influxdb.impl;

import org.influxdb.InfluxDBMapperException;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (21/06/2018 08:30)
 */
public abstract class AbstractInfluxDBMapper {

    private static final int FRACTION_MIN_WIDTH = 0;
    private static final int FRACTION_MAX_WIDTH = 9;
    private static final boolean ADD_DECIMAL_POINT = true;

    /**
     * Data structure used to cache classes used as measurements.
     */
    protected static final ConcurrentMap<String, ConcurrentMap<String, Field>> CLASS_FIELD_CACHE
            = new ConcurrentHashMap<>();

    /**
     * When a query is executed without {@link TimeUnit}, InfluxDB returns the <tt>time</tt>
     * column as an ISO8601 date.
     */
    protected static final DateTimeFormatter ISO8601_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, FRACTION_MIN_WIDTH, FRACTION_MAX_WIDTH, ADD_DECIMAL_POINT)
            .appendPattern("X")
            .toFormatter();

    @Nonnull
    protected String getMeasurementName(@Nonnull final Class<?> clazz) {

        Objects.requireNonNull(clazz, "Measurement clazz is required");

        Measurement measurement = clazz.getAnnotation(Measurement.class);
        if (measurement == null) {
            String message = String.format("Measurement type '%s' does not have a @Measurement annotation.", clazz);
            throw new InfluxDBMapperException(message);
        }

        return measurement.name();
    }

    protected void cacheMeasurementClass(@Nonnull final Class<?>... types) {
        for (Class<?> clazz : types) {
            if (CLASS_FIELD_CACHE.containsKey(clazz.getName())) {
                continue;
            }
            ConcurrentMap<String, Field> initialMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, Field> influxColumnAndFieldMap = CLASS_FIELD_CACHE
                    .putIfAbsent(clazz.getName(), initialMap);
            if (influxColumnAndFieldMap == null) {
                influxColumnAndFieldMap = initialMap;
            }

            for (Field field : clazz.getDeclaredFields()) {
                Column colAnnotation = field.getAnnotation(Column.class);
                if (colAnnotation != null) {
                    influxColumnAndFieldMap.put(colAnnotation.name(), field);
                }
            }
        }
    }

    /**
     * InfluxDB client returns any number as Double.
     * See https://github.com/influxdata/influxdb-java/issues/153#issuecomment-259681987
     * for more information.
     *
     * @param object
     * @param field
     * @param value
     * @param precision
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected <T> void setFieldValue(final T object, final Field field, final Object value,
                                     @Nonnull final TimeUnit precision)
            throws IllegalArgumentException, IllegalAccessException {

        Objects.requireNonNull(precision, "TimeUnit precision is required");

        if (value == null) {
            return;
        }
        Class<?> fieldType = field.getType();
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (fieldValueModified(fieldType, field, object, value, precision)
                    || fieldValueForPrimitivesModified(fieldType, field, object, value)
                    || fieldValueForPrimitiveWrappersModified(fieldType, field, object, value)) {
                return;
            }
            String msg = "Class '%s' field '%s' is from an unsupported type '%s'.";
            throw new InfluxDBMapperException(
                    String.format(msg, object.getClass().getName(), field.getName(), field.getType()));
        } catch (ClassCastException e) {
            String msg =
                    "Class '%s' field '%s' was defined with a different field type and caused a ClassCastException. "
                    + "The correct type is '%s' (current field value: '%s').";

            throw new InfluxDBMapperException(String.format(msg, object.getClass().getName(), field.getName(),
                    value.getClass().getName(), value));
        }
    }

    <T> boolean fieldValueModified(final Class<?> fieldType, final Field field, final T object, final Object value,
                                   @Nonnull final TimeUnit precision)
            throws IllegalArgumentException, IllegalAccessException {

        Objects.requireNonNull(precision, "TimeUnit precision is required");

        if (String.class.isAssignableFrom(fieldType)) {
            field.set(object, String.valueOf(value));
            return true;
        }
        if (Instant.class.isAssignableFrom(fieldType)) {
            Instant instant;
            if (value instanceof String) {
                instant = Instant.from(ISO8601_FORMATTER.parse(String.valueOf(value)));
            } else if (value instanceof Long) {
                instant = Instant.ofEpochMilli(toMillis((Long) value, precision));
            } else if (value instanceof Double) {
                instant = Instant.ofEpochMilli(toMillis(((Double) value).longValue(), precision));
            } else if (value instanceof Instant) {
                instant = (Instant) value;
            } else {
                String message = "Unsupported type " + field.getClass() + " for field " + field.getName();

                throw new InfluxDBMapperException(message);
            }
            field.set(object, instant);
            return true;
        }
        return false;
    }

    @Nonnull
    private Long toMillis(@Nonnull final Long value, @Nonnull final TimeUnit precision) {

        Objects.requireNonNull(precision, "TimeUnit precision is required");

        return TimeUnit.MILLISECONDS.convert(value, precision);
    }

    <T> boolean fieldValueForPrimitivesModified(final Class<?> fieldType, final Field field, final T object,
                                                final Object value)

            throws IllegalArgumentException, IllegalAccessException {

        if (double.class.isAssignableFrom(fieldType)) {
            field.setDouble(object, toDoubleValue(value));
            return true;
        }
        if (long.class.isAssignableFrom(fieldType)) {
            field.setLong(object, toLongValue(value));
            return true;
        }
        if (int.class.isAssignableFrom(fieldType)) {
            field.setInt(object, toIntValue(value));
            return true;
        }
        if (boolean.class.isAssignableFrom(fieldType)) {
            field.setBoolean(object, Boolean.valueOf(String.valueOf(value)).booleanValue());
            return true;
        }
        return false;
    }

    <T> boolean fieldValueForPrimitiveWrappersModified(final Class<?> fieldType, final Field field, final T object,
                                                       final Object value)
            throws IllegalArgumentException, IllegalAccessException {

        if (Double.class.isAssignableFrom(fieldType)) {
            field.set(object, value);
            return true;
        }
        if (Long.class.isAssignableFrom(fieldType)) {
            field.set(object, Long.valueOf(toLongValue(value)));
            return true;
        }
        if (Integer.class.isAssignableFrom(fieldType)) {
            field.set(object, Integer.valueOf(toIntValue(value)));
            return true;
        }
        if (Boolean.class.isAssignableFrom(fieldType)) {
            field.set(object, Boolean.valueOf(String.valueOf(value)));
            return true;
        }
        return false;
    }

    private double toDoubleValue(final Object value) {

        if (double.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
            return (double) value;
        }

        return ((Double) value).doubleValue();
    }

    private long toLongValue(final Object value) {

        if (long.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())) {
            return (long) value;
        }

        return ((Double) value).longValue();
    }

    private int toIntValue(final Object value) {

        if (int.class.isAssignableFrom(value.getClass()) || Integer.class.isAssignableFrom(value.getClass())) {
            return (int) value;
        }

        return ((Double) value).intValue();
    }
}
