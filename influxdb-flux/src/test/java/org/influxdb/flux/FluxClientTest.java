package org.influxdb.flux;

import okhttp3.logging.HttpLoggingInterceptor;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractFluxClientTest;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:24)
 */
class FluxClientTest extends AbstractFluxClientTest {

    @Test
    void gzip() {

        // default disabled
        Assertions.assertThat(fluxClient.isGzipEnabled()).isEqualTo(false);

        // enable
        fluxClient.enableGzip();
        Assertions.assertThat(fluxClient.isGzipEnabled()).isEqualTo(true);

        // disable
        fluxClient.disableGzip();
        Assertions.assertThat(fluxClient.isGzipEnabled()).isEqualTo(false);
    }

    @Test
    void logLevel() {

        // default NONE
        Assertions.assertThat(fluxClient.getLogLevel()).isEqualTo(HttpLoggingInterceptor.Level.NONE);

        // set HEADERS
        fluxClient.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);

        Assertions.assertThat(fluxClient.getLogLevel()).isEqualTo(HttpLoggingInterceptor.Level.HEADERS);
    }
}