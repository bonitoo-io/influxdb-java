package org.influxdb.flux;

import okhttp3.logging.HttpLoggingInterceptor;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractFluxReactiveTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:13)
 */
@RunWith(JUnitPlatform.class)
class FluxReactiveTest extends AbstractFluxReactiveTest {

    @Test
    void gzip() {

        // default disabled
        Assertions.assertThat(fluxReactive.isGzipEnabled()).isEqualTo(false);

        // enable
        fluxReactive.enableGzip();
        Assertions.assertThat(fluxReactive.isGzipEnabled()).isEqualTo(true);

        // disable
        fluxReactive.disableGzip();
        Assertions.assertThat(fluxReactive.isGzipEnabled()).isEqualTo(false);
    }

    @Test
    void logLevel() {

        // default NONE
        Assertions.assertThat(fluxReactive.getLogLevel()).isEqualTo(HttpLoggingInterceptor.Level.NONE);

        // set HEADERS
        fluxReactive.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);

        Assertions.assertThat(fluxReactive.getLogLevel()).isEqualTo(HttpLoggingInterceptor.Level.HEADERS);
    }

    @Test
    void close() {
        Assertions.assertThat(fluxReactive.isClosed()).isFalse();

        fluxReactive.close();

        Assertions.assertThat(fluxReactive.isClosed()).isTrue();
    }
}