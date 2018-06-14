package org.influxdb.reactive;

import org.influxdb.InfluxDBOptions;
import org.influxdb.impl.InfluxDBReactiveImpl;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The Factory that create a reactive instance of a InfluxDB client.
 *
 * @author Jakub Bednar (bednar@github) (12/06/2018 10:32)
 * @since 3.0.0
 */
public final class InfluxDBReactiveFactory {

    private InfluxDBReactiveFactory() {
    }

    /**
     * Create a instance of the InfluxDB reactive client.
     *
     * @param options the connection configuration
     * @return 3.0.0
     */
    @Nonnull
    public static InfluxDBReactive connect(@Nonnull final InfluxDBOptions options) {

        Objects.requireNonNull(options, "InfluxDBOptions is required");

        return connect(options, BatchOptionsReactive.DEFAULTS);
    }

    /**
     * Create a instance of the InfluxDB reactive client.
     *
     * @param options      the connection configuration
     * @param batchOptions the batch configuration
     * @return 3.0.0
     */
    @Nonnull
    public static InfluxDBReactive connect(@Nonnull final InfluxDBOptions options,
                                           @Nonnull final BatchOptionsReactive batchOptions) {

        Objects.requireNonNull(options, "InfluxDBOptions is required");
        Objects.requireNonNull(batchOptions, "BatchOptionsReactive is required");

        return new InfluxDBReactiveImpl(options, batchOptions);
    }
}
