package org.influxdb.flux.impl;

import io.reactivex.Flowable;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxReactive;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.mapper.FluxResultMapper;
import org.influxdb.flux.options.FluxOptions;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:59)
 */
public class FluxReactiveImpl implements FluxReactive {

    private final FluxResultMapper mapper = new FluxResultMapper();

    private final FluxOptions fluxOptions;

    public FluxReactiveImpl(@Nonnull final FluxOptions fluxOptions) {

        Objects.requireNonNull(fluxOptions, "FluxOptions are required");

        this.fluxOptions = fluxOptions;
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux flux, final @Nonnull Class<M> measurementType) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(flux, new HashMap<>(), measurementType);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux flux,
                                @Nonnull final Map<String, Object> parameters,
                                @Nonnull final Class<M> measurementType) {

        return flux(flux, parameters)
                .map(fluxResults -> mapper.toPOJO(fluxResults, measurementType))
                .concatMap(Flowable::fromIterable);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux flux) {

        Objects.requireNonNull(flux, "Flux is required");

        return flux(flux, new HashMap<>());
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux flux, @Nonnull final Map<String, Object> parameters) {
        throw new IllegalStateException("Not implemented");
    }
}
