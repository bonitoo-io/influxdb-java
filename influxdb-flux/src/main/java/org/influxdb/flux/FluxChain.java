package org.influxdb.flux;

import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.temporal.ChronoUnit;
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
     * Add the duration Flux parameter.
     *
     * @param name       the parameter name
     * @param amount     the amount of the duration, measured in terms of the unit, positive or negative
     * @param chronoUnit the unit that the duration is measured in, must have an exact duration, not null
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain addParameter(@Nonnull final String name, final long amount, @Nonnull final ChronoUnit chronoUnit) {

        return addParameter(name, new TimeInterval(amount, chronoUnit));
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

    public static class TimeInterval {

        private static final int HOURS_IN_HALF_DAY = 12;

        private Long interval;
        private ChronoUnit chronoUnit;

        public TimeInterval(@Nullable final Long interval, @Nullable final ChronoUnit chronoUnit) {
            this.interval = interval;
            this.chronoUnit = chronoUnit;
        }

        @Nullable
        public String value() {

            if (interval == null || chronoUnit == null) {
                return null;
            }

            String unit;
            Long calculatedInterval = interval;
            switch (chronoUnit) {
                case NANOS:
                    unit = "n";
                    break;
                case MICROS:
                    unit = "u";
                    break;
                case MILLIS:
                    unit = "ms";
                    break;
                case SECONDS:
                    unit = "s";
                    break;
                case MINUTES:
                    unit = "m";
                    break;
                case HOURS:
                    unit = "h";
                    break;
                case HALF_DAYS:
                    unit = "h";
                    calculatedInterval = HOURS_IN_HALF_DAY * interval;
                    break;
                case DAYS:
                    unit = "d";
                    break;
                case WEEKS:
                    unit = "w";
                    break;
                default:
                    String message = "Unit must be one of: "
                            + "NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, HALF_DAYS, DAYS, WEEKS";

                    throw new IllegalArgumentException(message);
            }

            return new StringBuilder()
                    .append(calculatedInterval)
                    .append(unit).toString();
        }
    }
}
