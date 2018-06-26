package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.flux.Preconditions;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Abstract base class for operators that take an upstream source of {@link Flux}.
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 07:29)
 */
abstract class AbstractFluxWithUpstream extends Flux {

    Flux source;

    private DateTimeFormatter dataFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'")
            .withZone(ZoneId.of("UTC"));

    AbstractFluxWithUpstream(@Nonnull final Flux source) {

        Objects.requireNonNull(source, "Source is required");

        this.source = source;
    }

    @Override
    protected final void appendActual(@Nonnull final FluxChain fluxChain) {
        fluxChain.append(source);

        appendAfterUpstream(fluxChain);
    }

    /**
     * Append the actual operator to {@link FluxChain}.
     *
     * @param fluxChain the incoming {@link FluxChain}, never null
     */
    abstract void appendAfterUpstream(@Nonnull final FluxChain fluxChain);

    void appendParameters(@Nonnull final StringBuilder operator,
                          @Nonnull final FluxChain fluxChain,
                          @Nonnull final NamedParameter... parameters) {

        boolean wasAppended = false;
        for (NamedParameter named : parameters) {

            wasAppended = appendParameterTo(named, operator, fluxChain, wasAppended);
        }
    }

    class NamedParameter {

        private String name;
        private FluxChain.FluxParameter parameter;

        NamedParameter(@Nonnull final String name, @Nonnull final FluxChain.FluxParameter parameter) {

            Preconditions.checkNonEmptyString(name, "Parameter name");
            Objects.requireNonNull(parameter, "FluxParameter is required");

            this.name = name;
            this.parameter = parameter;
        }
    }

    class TimeInterval {
        private Long interval;
        private TimeUnit unit;

        TimeInterval(@Nonnull final Long interval, @Nonnull final TimeUnit unit) {
            this.interval = interval;
            this.unit = unit;
        }
    }

    /**
     * @return {@link Boolean#TRUE} if was appended parameter
     */
    private boolean appendParameterTo(@Nonnull final NamedParameter namedParameter,
                                      @Nonnull final StringBuilder operator,
                                      @Nonnull final FluxChain fluxChain,
                                      final boolean wasAppendParameter) {

        if (namedParameter.parameter instanceof FluxChain.NotDefinedParameter) {
            return wasAppendParameter;
        }

        Object parameterValue = namedParameter.parameter.value(fluxChain.getParameters());

        // array to collection
        if (parameterValue.getClass().isArray()) {
            parameterValue = Arrays.asList((Object[]) parameterValue);
        }

        // collection to delimited string ["one", "two", "three"]
        if (parameterValue instanceof Collection) {

            Collection collection = (Collection) parameterValue;
            if (collection.isEmpty()) {
                return wasAppendParameter;
            }

            parameterValue = collection.stream()
                    .map(host -> "\"" + host + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        if (parameterValue instanceof Instant) {
            parameterValue = dataFormat.format((Instant) parameterValue);
        }

        if (parameterValue instanceof TimeInterval) {
            String timeInterval = ((TimeInterval) parameterValue).interval.toString();
            timeInterval += toTimePrecision(((TimeInterval) parameterValue).unit);
            parameterValue = timeInterval;
        }

        // delimit previously appended parameter
        if (wasAppendParameter) {
            operator.append(", ");
        }

        // n: 5
        operator
                .append(namedParameter.name)
                .append(": ")
                .append(parameterValue.toString());

        return true;
    }


    // TODO reuse from core - new module?
    @Nonnull
    private String toTimePrecision(@Nonnull final TimeUnit timeUnit) {
        Objects.requireNonNull(timeUnit, "TimeUnit is required");

        switch (timeUnit) {
            case HOURS:
                return "h";
            case MINUTES:
                return "m";
            case SECONDS:
                return "s";
            case MILLISECONDS:
                return "ms";
            case MICROSECONDS:
                return "u";
            case NANOSECONDS:
                return "n";
            default:
                throw new IllegalArgumentException("time precision must be one of");
        }
    }
}
