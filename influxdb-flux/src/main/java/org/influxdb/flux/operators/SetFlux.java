package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#yield">yield</a> - Assigns a static
 * value to each record.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>key</b> - Label for the column to set [string].</li>
 * <li><b>value</b> - Value for the column to set [string]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .set("location", "Carolina");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 09:19)
 * @since 3.0.0
 */
public final class SetFlux extends AbstractParametrizedFlux {

    public SetFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "set";
    }

    /**
     * @param key   label for the column. Has to be defined.
     * @param value value for the column. Has to be defined.
     * @return this
     */
    @Nonnull
    public SetFlux withKeyValue(@Nonnull final String key, @Nonnull final String value) {

        Preconditions.checkNonEmptyString(key, "Key");
        Preconditions.checkNonEmptyString(value, "Value");

        withPropertyValueEscaped("key", key);
        withPropertyValueEscaped("value", value);

        return this;
    }
}
