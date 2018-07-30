package org.influxdb.flux.operators.properties;

import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Flux duration literal -
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#duration-literals">spec</a>.
 * <p>
 * A duration literal is a representation of a length of time. It has an integer part and a duration unit part.
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 06:40)
 */
public class TimeInterval {

    private static final int HOURS_IN_HALF_DAY = 12;

    private Long interval;
    private ChronoUnit chronoUnit;

    public TimeInterval(@Nonnull final Long interval, @Nonnull final ChronoUnit chronoUnit) {

        Objects.requireNonNull(interval, "Interval is required");
        Objects.requireNonNull(chronoUnit, "ChronoUnit is required");

        this.interval = interval;
        this.chronoUnit = chronoUnit;
    }

    @Override
    public String toString() {

        String unit;
        Long calculatedInterval = interval;
        switch (chronoUnit) {
            case NANOS:
                unit = "ns";
                break;
            case MICROS:
                unit = "us";
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

        return String.valueOf(calculatedInterval) + unit;
    }
}
