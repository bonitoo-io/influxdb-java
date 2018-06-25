package org.influxdb.flux;

import org.influxdb.flux.operators.CountFlux;
import org.influxdb.flux.operators.FromFlux;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#basic-syntax">Flux</a> -
 * Functional Language for defining a query to execute.
 * <h3>The operators:</h3>
 * <ul>
 * <li>{@link FromFlux}</li>
 * <li>{@link CountFlux}</li>
 * <li>filter - UNSUPPORTED</li>
 * <li>first - UNSUPPORTED</li>
 * <li>group - UNSUPPORTED</li>
 * <li>join - UNSUPPORTED</li>
 * <li>last - UNSUPPORTED</li>
 * <li>limit - UNSUPPORTED</li>
 * <li>map - UNSUPPORTED</li>
 * <li>max - UNSUPPORTED</li>
 * <li>mean - UNSUPPORTED</li>
 * <li>min - UNSUPPORTED</li>
 * <li>range - UNSUPPORTED</li>
 * <li>sample - UNSUPPORTED</li>
 * <li>set - UNSUPPORTED</li>
 * <li>skew - UNSUPPORTED</li>
 * <li>sort - UNSUPPORTED</li>
 * <li>spread - UNSUPPORTED</li>
 * <li>stddev - UNSUPPORTED</li>
 * <li>sum - UNSUPPORTED</li>
 * <li>toBool - UNSUPPORTED</li>
 * <li>toInt - UNSUPPORTED</li>
 * <li>toFloat - UNSUPPORTED</li>
 * <li>toDuration - UNSUPPORTED</li>
 * <li>toString - UNSUPPORTED</li>
 * <li>toTime - UNSUPPORTED</li>
 * <li>toUInt - UNSUPPORTED</li>
 * <li>toUInt - UNSUPPORTED</li>
 * <li>window - UNSUPPORTED</li>
 * <li>toHttp - UNSUPPORTED</li>
 * <li>toKafka - UNSUPPORTED</li>
 * <li>own - UNSUPPORTED</li>
 * </ul>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:16)
 * @since 3.0.0
 */
public abstract class Flux {

    /**
     * Get data from the specified database.
     *
     * @param db database name
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String db) {
        Preconditions.checkNonEmptyString(db, "Database name");

        return new FromFlux(db);
    }

    /**
     * Get data from the specified database.
     *
     * @param db    database name
     * @param hosts the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String db, @Nonnull final Collection<String> hosts) {
        Preconditions.checkNonEmptyString(db, "Database name");
        Objects.requireNonNull(hosts, "Hosts are required");

        return new FromFlux(db, hosts);
    }

    /**
     * Get data from the specified database.
     *
     * @param db    database name
     * @param hosts the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String db, @Nonnull final String[] hosts) {
        Preconditions.checkNonEmptyString(db, "Database name");
        Objects.requireNonNull(hosts, "Hosts are required");

        return new FromFlux(db, hosts);
    }

    /**
     * Counts the number of results.
     *
     * @return {@link CountFlux}
     */
    @Nonnull
    public Flux count() {
        return new CountFlux(this);
    }

    /**
     * Counts the number of results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link CountFlux}
     */
    @Nonnull
    public Flux count(final boolean useStartTime) {
        return new CountFlux(this, useStartTime);
    }

    /**
     * Append the actual operator to {@link FluxChain}.
     *
     * @param fluxChain the incoming {@link FluxChain}, never null
     */
    protected abstract void appendActual(@Nonnull final FluxChain fluxChain);

    /**
     * Create the Flux script that can be executed in {@code fluxd}.
     *
     * @return Flux script
     */
    @Nonnull
    public String print() {
        FluxChain fluxChain = new FluxChain();
        appendActual(fluxChain);
        return fluxChain.print();
    }

    @Override
    public String toString() {
        return print();
    }
}
