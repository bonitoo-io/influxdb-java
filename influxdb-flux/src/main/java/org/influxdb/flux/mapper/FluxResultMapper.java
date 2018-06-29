package org.influxdb.flux.mapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import okio.Buffer;
import okio.BufferedSource;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:04)
 */
@ThreadSafe
public class FluxResultMapper {

    @Nullable
    public FluxResult toFluxResult(@Nonnull final BufferedSource source) throws FluxResultMapperException, IOException {
        Objects.requireNonNull(source, "BufferedSource is required");

        Buffer buffer = new Buffer();
        source.readAll(buffer);
        Reader reader = new InputStreamReader(buffer.inputStream());
        FluxCsvParser tableCsvParser = new FluxCsvParser();

        return tableCsvParser.parseFluxResponse(reader);
    }

    @Nonnull
    public <T> List<T> toPOJO(@Nonnull final FluxResult fluxResult, @Nonnull final Class<T> type) {

        Objects.requireNonNull(fluxResult, "FluxResults is required");
        Objects.requireNonNull(type, "Class type is required");

        List<T> results = new ArrayList<>();
        try {
            results.add(type.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
