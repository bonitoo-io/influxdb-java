package org.influxdb.flux;

import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The utility for chaining Flux operators {@link org.influxdb.flux.operators}.
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 11:14)
 */
public final class FluxChain {

    private final StringBuilder builder = new StringBuilder();

    private Map<String, Object> parameters = new HashMap<>();

    public FluxChain() {
    }

    /**
     * Add the Flux parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain addParameter(@Nonnull final String name, @Nonnull final Object value) {

        Preconditions.checkNonEmptyString(name, "Parameter name");
        Objects.requireNonNull(value, "Parameter value is required");

        parameters.put(name, value);

        return this;
    }

    /**
     * Add the Flux parameters.
     *
     * @param parameters parameters
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain addParameters(@Nonnull final Map<String, Object> parameters) {

        Objects.requireNonNull(parameters, "Parameters are required");

        this.parameters.putAll(parameters);

        return this;
    }

    /**
     * @return get bound parameters
     */
    @Nonnull
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Appends the operator to the chain sequence.
     *
     * @param operator the incoming operator
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain append(@Nullable final CharSequence operator) {

        if (operator == null) {
            return this;
        }

        if (builder.length() != 0) {
            builder.append("\n");
            builder.append("\t|> ");
        }
        builder.append(operator);

        return this;
    }

    /**
     * Appends the {@code source} to the chain sequence.
     *
     * @param source the incoming {@link Flux} operator
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain append(@Nonnull final Flux source) {

        Objects.requireNonNull(source, "Flux source is required");

        source.appendActual(this);

        return this;
    }

    /**
     * @return operator chain
     */
    @Nonnull
    String print() {
        return builder.toString();
    }

    /**
     * The source of parameter value.
     */
    public interface FluxParameter<T> {

        /**
         * @param parameters bounded parameters
         * @return value of parameter
         */
        @Nonnull
        T value(@Nonnull final Map<String, Object> parameters);
    }

    public static class BoundFluxParameter<T> implements FluxParameter<T> {

        private final String parameterName;

        public BoundFluxParameter(@Nonnull final String parameterName) {

            Preconditions.checkNonEmptyString(parameterName, "Parameter name");

            this.parameterName = parameterName;
        }

        @Nonnull
        @Override
        public T value(@Nonnull final Map<String, Object> parameters) {

            Object parameterValue = parameters.get(parameterName);
            // parameter must be defined
            if (parameterValue == null) {
                String message = String.format("The parameter '%s' is not defined.", parameterName);

                throw new IllegalStateException(message);
            }

            //noinspection unchecked
            return (T) parameterValue;
        }
    }

    public static class NotDefinedParameter<T> implements FluxParameter<T> {
        @Nonnull
        @Override
        public T value(@Nonnull final Map<String, Object> parameters) {
            throw new IllegalStateException();
        }
    }
}
