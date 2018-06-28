package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#filter">filter</a> -
 * Filters the results using an expression.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 *     <b>fn</b> - Function to when filtering the records. The function must accept a single parameter
 *     which will be the records and return a boolean value. Records which evaluate to true,
 *     will be included in the results. [function(record) bool]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *  Restrictions restriction = Restrictions.and(
 *          Restrictions.measurement().equal("mem"),
 *          Restrictions.field().equal("usage_system"),
 *          Restrictions.tag("service").equal("app-server")
 * );
 *
 * Flux flux = Flux
 *          .from("telegraf")
 *          .filter(restriction)
 *          .range(-4L, ChronoUnit.HOURS)
 *          .count();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 14:12)
 * @since 3.0.0
 */
public final class FilterFlux extends AbstractParametrizedFlux {

    public FilterFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "filter";
    }
}
