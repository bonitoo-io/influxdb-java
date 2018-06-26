package org.influxdb.flux.mapper;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:04)
 */
@ThreadSafe
public class FluxResultMapper {

    @Nonnull
    public <T> List<T> toPOJO(@Nonnull final FluxResult fluxResult, @Nonnull final Class<T> type) {

        Objects.requireNonNull(fluxResult, "FluxResults is required");
        Objects.requireNonNull(type, "Class type is required");

        return new ArrayList<>();
    }
}
