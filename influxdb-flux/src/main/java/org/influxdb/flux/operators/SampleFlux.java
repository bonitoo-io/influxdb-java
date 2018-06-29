package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#sample">sample</a> - Sample values from a table.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>n</b> - Sample every Nth element [int]</li>
 * <li>
 * <b>pos</b> - Position offset from start of results to begin sampling. <i>pos</i> must be less than <i>n</i>.
 * If <i>pos</i> less than 0, a random offset is used. Default is -1 (random offset) [int].
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux.from("telegraf")
 *     .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
 *     .range(-1L, ChronoUnit.DAYS)
 *     .sample(10);
 *
 * Flux flux = Flux.from("telegraf")
 *     .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
 *     .range(-1L, ChronoUnit.DAYS)
 *     .sample(5, 1);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 07:25)
 * @since 3.0.0
 */
public final class SampleFlux extends AbstractParametrizedFlux {

    public SampleFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "sample";
    }

    /**
     * @param n Sample every Nth element.
     * @return this
     */
    @Nonnull
    public SampleFlux withN(final int n) {
        addPropertyValue("n", n);

        return this;
    }

    /**
     * @param pos Position offset from start of results to begin sampling. Must be less than @{code n}.
     * @return this
     */
    @Nonnull
    public SampleFlux withPos(final int pos) {
        addPropertyValue("pos", pos);

        return this;
    }
}
