package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#join">join</a> -
 * Join two time series together on time and the list of <i>on</i> keys.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>tables</b> - Map of tables to join. Currently only two tables are allowed. [map of tables]</li>
 * <li><b>on</b> - List of tag keys that when equal produces a result set. [array of strings]</li>
 * <li>
 * <b>fn</b> - Defines the function that merges the values of the tables.
 * The function must defined to accept a single parameter.
 * The parameter is a map, which uses the same keys found in the tables map.
 * The function is called for each joined set of records from the tables. [function(tables)]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 14:47)
 * @since 3.0.0
 */
public final class JoinFlux extends AbstractParametrizedFlux {

    public JoinFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "join";
    }
}
