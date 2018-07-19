package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#map">map</a> - Applies a function to
 * each row of the table.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>fn</b> - The function to apply to each row.
 * The return value of the function may be a single value or an object. [function(record) value]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Restrictions restriction = Restrictions.and(
 *     Restrictions.measurement().equal("cpu"),
 *     Restrictions.field().equal("usage_system"),
 *     Restrictions.tag("service").equal("app-server")
 * );
 *
 * // Square the value
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(restriction)
 *     .range(-12L, ChronoUnit.HOURS)
 *     .map("r._value * r._value");
 *
 * // Square the value and keep the original value
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(restriction)
 *     .range(-12L, ChronoUnit.HOURS)
 *     .map("{value: r._value, value2:r._value * r._value}");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 07:48)
 * @since 3.0.0
 */
public final class MapFlux extends AbstractParametrizedFlux {

    public MapFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "map";
    }

    @Nonnull
    @Override
    protected String propertyDelimiter(@Nonnull final String operatorName) {
        return " => ";
    }

    /**
     * @param function The function for map row of table. Example: "r._value * r._value".
     * @return this
     */
    @Nonnull
    public MapFlux withFunction(@Nonnull final String function) {

        Preconditions.checkNonEmptyString(function, "Function");

        this.withPropertyValue("fn: (r)", function);

        return this;
    }
}
