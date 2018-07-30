package org.influxdb.impl;

import okhttp3.logging.HttpLoggingInterceptor;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxClient;
import org.influxdb.flux.events.AbstractFluxEvent;
import org.influxdb.flux.events.UnhandledErrorEvent;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.options.FluxConnectionOptions;
import org.influxdb.flux.options.FluxOptions;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (30/07/2018 13:56)
 */
public class FluxClientImpl extends AbstractFluxClient<FluxService> implements FluxClient {

    private static final Logger LOG = Logger.getLogger(FluxClientImpl.class.getName());

    private final Map<Class<?>, Set<Consumer>> subscribers = new ConcurrentHashMap<>();

    FluxClientImpl(@Nonnull final FluxConnectionOptions options) {
        super(options, FluxService.class, null);
    }

    @Nonnull
    @Override
    public <M> List<M> flux(@Nonnull final Flux query, @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(query, measurementType, FluxOptions.DEFAULTS);
    }

    @Override
    public <M> void flux(@Nonnull final Flux query,
                         @Nonnull final Class<M> measurementType,
                         @Nonnull final Consumer<List<M>> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, new HashMap<>(), measurementType, callback);
    }

    @Nonnull
    @Override
    public <M> List<M> flux(@Nonnull final Flux query,
                            @Nonnull final Class<M> measurementType,
                            @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, new HashMap<>(), measurementType, options);
    }

    @Override
    public <M> void flux(@Nonnull final Flux query,
                         @Nonnull final Class<M> measurementType,
                         @Nonnull final FluxOptions options,
                         @Nonnull final Consumer<List<M>> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, new HashMap<>(), measurementType, options, callback);
    }

    @Nonnull
    @Override
    public <M> List<M> flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(query, properties, measurementType, FluxOptions.DEFAULTS);
    }

    @Override
    public <M> void flux(@Nonnull final Flux query,
                         @Nonnull final Map<String, Object> properties,
                         @Nonnull final Class<M> measurementType,
                         @Nonnull final Consumer<List<M>> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, properties, measurementType, FluxOptions.DEFAULTS, callback);
    }

    @Nonnull
    @Override
    public <M> List<M> flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final Class<M> measurementType,
                            @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(options, "FluxOptions are required");

        FluxResult fluxResult = flux(query, properties, options);

        return mapper.toPOJO(fluxResult, measurementType);
    }

    @Override
    public <M> void flux(@Nonnull final Flux query,
                         @Nonnull final Map<String, Object> properties,
                         @Nonnull final Class<M> measurementType,
                         @Nonnull final FluxOptions options,
                         @Nonnull final Consumer<List<M>> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, properties, options, fluxResult -> callback.accept(mapper.toPOJO(fluxResult, measurementType)));
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query) {

        Objects.requireNonNull(query, "Flux is required");

        return flux(query, FluxOptions.DEFAULTS);
    }

    @Override
    public void flux(@Nonnull final Flux query, @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, FluxOptions.DEFAULTS, callback);
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query, @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, new HashMap<>(), options);
    }

    @Override
    public void flux(@Nonnull final Flux query,
                     @Nonnull final FluxOptions options,
                     @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, new HashMap<>(), options, callback);
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");

        return flux(query, properties, FluxOptions.DEFAULTS);
    }

    @Override
    public void flux(@Nonnull final Flux query,
                     @Nonnull final Map<String, Object> properties,
                     @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, properties, FluxOptions.DEFAULTS, callback);
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query,
                           @Nonnull final Map<String, Object> properties,
                           @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, properties, options, false, result -> {
        });
    }

    @Override
    public void flux(@Nonnull final Flux query,
                     @Nonnull final Map<String, Object> properties,
                     @Nonnull final FluxOptions options,
                     @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, properties, options, true, callback);
    }

    private FluxResult flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Boolean async,
                            @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(async, "Async configuration is required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        throw new IllegalStateException("Not implemented");
    }

    @Override
    public <T extends AbstractFluxEvent> void listenEvents(@Nonnull final Class<T> eventType,
                                                           @Nonnull final Consumer<T> listener) {

        Objects.requireNonNull(eventType, "Event type is required");
        Objects.requireNonNull(listener, "Consumer is required");

        Set<Consumer> listeners = subscribers.get(eventType);
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
            subscribers.put(eventType, listeners);
        }

        listeners.add(listener);
    }

    @Override
    public <T extends AbstractFluxEvent> void unsubscribeEvents(@Nonnull final Consumer<T> listener) {

        Objects.requireNonNull(listener, "Consumer is required");

        subscribers.values().forEach(listeners -> listeners.remove(listener));
    }

    @Nonnull
    @Override
    public FluxClient enableGzip() {

        this.gzipRequestInterceptor.enable();

        return this;
    }

    @Nonnull
    @Override
    public FluxClient disableGzip() {

        this.gzipRequestInterceptor.disable();

        return this;
    }

    @Override
    public boolean isGzipEnabled() {
        return this.gzipRequestInterceptor.isEnabled();
    }

    @Nonnull
    @Override
    public Boolean ping() {

        try {
            return fluxService.ping().isSuccessful();
        } catch (Exception e) {
            publish(new UnhandledErrorEvent(e));
        }

        return false;
    }

    @Nonnull
    @Override
    public HttpLoggingInterceptor.Level getLogLevel() {

        return this.loggingInterceptor.getLevel();
    }

    @Nonnull
    @Override
    public FluxClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel) {

        Objects.requireNonNull(logLevel, "Log level is required");

        this.loggingInterceptor.setLevel(logLevel);

        return this;
    }

    @Nonnull
    @Override
    public FluxClient close() {

        LOG.log(Level.INFO, "Dispose all event listeners before shutdown.");

        subscribers.clear();

        return this;
    }

    private void publish(@Nonnull final AbstractFluxEvent event) {

        Objects.requireNonNull(event, "Event is required");

        event.logEvent();

        Class<?> eventType = event.getClass();

        //noinspection unchecked
        subscribers.keySet().stream()
                .filter(type -> type.isAssignableFrom(eventType))
                .flatMap(type -> subscribers.get(type).stream())
                .forEach(consumer -> consumer.accept(event));
    }
}
