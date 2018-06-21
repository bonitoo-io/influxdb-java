package org.influxdb.impl;

import org.influxdb.InfluxDBMapperException;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jakub Bednar (bednar@github) (21/06/2018 08:30)
 */
abstract class AbstractInfluxDBMapper {

    /**
     * Data structure used to cache classes used as measurements.
     */
    static final ConcurrentMap<String, ConcurrentMap<String, Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();

    @Nonnull
    String getMeasurementName(@Nonnull final Class<?> clazz) {

        Objects.requireNonNull(clazz, "Measurement clazz is required");

        Measurement measurement = clazz.getAnnotation(Measurement.class);
        if (measurement == null) {
            String message = String.format("Measurement type '%s' does not have a @Measurement annotation.", clazz);
            throw new InfluxDBMapperException(message);
        }

        return measurement.name();
    }

    void cacheMeasurementClass(@Nonnull final Class<?>... types) {
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
}
