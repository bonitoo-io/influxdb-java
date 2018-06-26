package org.influxdb.flux;

import io.reactivex.Flowable;
import org.influxdb.flux.mapper.FluxResult;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * The client for the Flux service.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:07)
 * @since 3.0.0
 */
public interface FluxReactive {

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux            the flux query to execute
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a {@link FluxResult} mapped to {@code measurementType} which are matched
     * the query or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> flux(@Nonnull final Flux flux, @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux            the flux query to execute
     * @param parameters      named parameters
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a {@link FluxResult} mapped to {@code measurementType} which are matched
     * the query or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> flux(@Nonnull final Flux flux,
                         @Nonnull final Map<String, Object> parameters,
                         @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux       the flux query to execute
     * @return {@link Flowable} emitting a {@link FluxResult} which are matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<FluxResult> flux(@Nonnull final Flux flux);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux       the flux query to execute
     * @param parameters named parameters
     * @return {@link Flowable} emitting a {@link FluxResult} which are matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<FluxResult> flux(@Nonnull final Flux flux, @Nonnull final Map<String, Object> parameters);
}
