package org.influxdb.flux.mapper;

import okio.Buffer;
import okio.BufferedSource;
import org.influxdb.annotation.Column;
import org.influxdb.impl.AbstractInfluxDBMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:04)
 */
@ThreadSafe
public class FluxResultMapper extends AbstractInfluxDBMapper {

    private static final Logger LOG = Logger.getLogger(FluxResultMapper.class.getName());

    @Nullable
    public FluxResult toFluxResult(@Nonnull final BufferedSource source)
            throws FluxResultMapperException, IOException {

        Objects.requireNonNull(source, "BufferedSource is required");

        Buffer buffer = new Buffer();
        source.readAll(buffer);
        Reader reader = new InputStreamReader(buffer.inputStream());
        FluxCsvParser tableCsvParser = new FluxCsvParser();

        return tableCsvParser.parseFluxResponse(reader);
    }

    /**
     * Mapping {@link FluxResult} to POJO measurements.
     *
     * @param fluxResult the Flux result object
     * @param type       the Class that will be used to hold your measurement data
     * @param <T>        the measurement type
     * @return a {@link List} of measurements from the same Class passed as parameter and sorted on the
     * same order as received from Flux.
     * @throws FluxResultMapperException if the {@code fluxResult} can't be mapped to {@code type}
     */
    @Nonnull
    public <T> List<T> toPOJO(@Nonnull final FluxResult fluxResult, @Nonnull final Class<T> type)
            throws FluxResultMapperException {

        Objects.requireNonNull(fluxResult, "FluxResults is required");
        Objects.requireNonNull(type, "Measurement type is required");

        cacheMeasurementClass(type);

        List<T> results = new ArrayList<>();

        String measurementName = getMeasurementName(type);

        List<Table> tables = fluxResult.getTables();
        for (Table table : tables) {

            table.getRecords()
                    .stream()
                    .filter(record -> record.getMeasurement().equals(measurementName))
                    .forEach(record -> {

                        T measurement = toPOJO(record, type);

                        results.add(measurement);
                    });
        }

        return results;
    }

    @Nonnull
    private <T> T toPOJO(@Nonnull final Record record, @Nonnull final Class<T> measurementType)
            throws FluxResultMapperException {

        Objects.requireNonNull(record, "Record is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");

        try {
            T measurement = measurementType.newInstance();

            CLASS_FIELD_CACHE.get(measurementType.getName()).forEach((name, field) -> {

                Column column = field.getAnnotation(Column.class);

                Class<?> fieldType = field.getType();

                // tags
                if (column.tag()) {
                    setFieldValue(measurement, field, record.getTags().get(column.name()));
                // field
                } else if (column.name().equals(record.getField())) {
                    setFieldValue(measurement, field, record.getValue());
                // time
                } else if (Instant.class.isAssignableFrom(fieldType)) {
                    setFieldValue(measurement, field, record.getTime());
                }
            });

            return measurement;
        } catch (Exception e) {
            throw new FluxResultMapperException(e);
        }
    }

    private void setFieldValue(@Nonnull final Object object,
                               @Nullable final Field field,
                               @Nullable final Object value) {

        if (field == null || value == null) {
            Object[] params = {field, value, object};
            LOG.log(Level.FINEST, "Field: {0} or Value: {1} is null for Object: {3}.", params);
            return;
        }

        try {
            setFieldValue(object, field, value, TimeUnit.NANOSECONDS);
        } catch (IllegalAccessException e) {
            throw new FluxResultMapperException(e);
        }
    }
}
