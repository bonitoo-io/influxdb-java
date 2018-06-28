package org.influxdb.flux.mapper;

import okio.BufferedSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:04)
 */
@ThreadSafe
public class FluxResultMapper {

    @Nullable
    public FluxResult toFluxResult(@Nonnull final BufferedSource source) {

        Objects.requireNonNull(source, "BufferedSource is required");

        try {
            return new FluxResult(source.readUtf8());
        } catch (IOException e) {
            return new FluxResult(null);
        }
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
