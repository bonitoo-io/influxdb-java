package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 14:03)
 */
abstract class AbstractParametrizedFlux extends AbstractFluxWithUpstream {

    private final DateTimeFormatter dataFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'")
            .withZone(ZoneId.of("UTC"));

    AbstractParametrizedFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Override
    protected final void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder operator = new StringBuilder();
        //
        // operator(
        //
        operator.append(operatorName()).append("(");
        //
        //
        // parameters: false
        boolean wasAppended = false;
        for (NamedParameter named : getParameters()) {

            wasAppended = appendParameterTo(named, operator, fluxChain, wasAppended);
        }
        //
        // )
        //
        operator.append(")");

        fluxChain.append(operator);
    }

    /**
     * @return name of operator
     */
    @Nonnull
    abstract String operatorName();

    /**
     * @return get parameters of operator
     */
    @Nonnull
    abstract List<NamedParameter> getParameters();

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
        if (parameterValue == null) {
            return wasAppendParameter;
        }

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
            parameterValue = ((TimeInterval) parameterValue).value();
        }

        if (parameterValue == null) {
            return wasAppendParameter;
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

        private static final int HOURS_IN_HALF_DAY = 12;

        private Long interval;
        private ChronoUnit chronoUnit;

        TimeInterval(@Nullable final Long interval, @Nullable final ChronoUnit chronoUnit) {
            this.interval = interval;
            this.chronoUnit = chronoUnit;
        }

        @Nullable
        String value() {

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
