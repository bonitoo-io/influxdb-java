package org.influxdb.flux;

import okhttp3.logging.HttpLoggingInterceptor;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractFluxClientReactiveTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:13)
 */
@RunWith(JUnitPlatform.class)
class FluxClientReactiveTest extends AbstractFluxClientReactiveTest {

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

    @Test
    void close() {
        Assertions.assertThat(fluxClient.isClosed()).isFalse();

        fluxClient.close();

        Assertions.assertThat(fluxClient.isClosed()).isTrue();
    }
}