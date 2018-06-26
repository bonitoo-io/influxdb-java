package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#from">from</a> - starting point
 * for all queries. Get data from the specified database.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>db</b> - string from(db:"telegraf")</li>
 * <li><b>hosts</b> - array of strings from(db:"telegraf", hosts:["host1", "host2"])</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db:"telegraf")
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:20)
 * @since 3.0.0
 */
public final class FromFlux extends Flux {

    private String db;
    private Collection<String> hosts;

    public FromFlux(@Nonnull final String db) {
        Preconditions.checkNonEmptyString(db, "Database name");

        this.db = db;
    }

    public FromFlux(@Nonnull final String db, @Nonnull final String[] hosts) {
        Preconditions.checkNonEmptyString(db, "Database name");
        Objects.requireNonNull(hosts, "Hosts are required");

        this.db = db;
        this.hosts = Arrays.asList(hosts);
    }

    public FromFlux(@Nonnull final String db, @Nonnull final Collection<String> hosts) {
        Preconditions.checkNonEmptyString(db, "Database name");
        Objects.requireNonNull(hosts, "Hosts are required");

        this.db = db;
        this.hosts = hosts;
    }

    @Override
    protected void appendActual(@Nonnull final FluxChain fluxChain) {

        //
        // from(db:"telegraf"
        //
        StringBuilder fromBuilder = new StringBuilder()
                .append("from(db:\"")
                .append(db)
                .append("\"");

        //
        // , hosts:["host1", "host2"]
        //
        if (hosts != null && !hosts.isEmpty()) {

            String concatenatedHosts = hosts.stream().map(host -> "\"" + host + "\"")
                    .collect(Collectors.joining(", "));

            fromBuilder.append(", hosts:[");
            fromBuilder.append(concatenatedHosts);
            fromBuilder.append("]");
        }

        //
        // )
        //
        fromBuilder.append(")");

        fluxChain.append(fromBuilder);
    }
}
