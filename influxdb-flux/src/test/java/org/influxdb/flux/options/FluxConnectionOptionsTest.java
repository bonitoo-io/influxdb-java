package org.influxdb.flux.options;

import okhttp3.OkHttpClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 09:09)
 */
@RunWith(JUnitPlatform.class)
class FluxConnectionOptionsTest {

    @Test
    void value() {
        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .orgID("00")
                .build();

        Assertions.assertThat(fluxConnectionOptions.getUrl()).isEqualTo("http://localhost:8093");
        Assertions.assertThat(fluxConnectionOptions.getOrgID()).isEqualTo("00");
        Assertions.assertThat(fluxConnectionOptions.getOkHttpClient()).isNotNull();
    }

    @Test
    void urlRequired() {

        FluxConnectionOptions.Builder fluxConnectionOptions = FluxConnectionOptions.builder().orgID("00");

        Assertions.assertThatThrownBy(fluxConnectionOptions::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The url to connect to Flux has to be defined.");
    }

    @Test
    void orgIdRequired() {

        FluxConnectionOptions.Builder fluxConnectionOptions = FluxConnectionOptions.builder().url("http://localhost:8093");

        Assertions.assertThatThrownBy(fluxConnectionOptions::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The organization id required by Flux has to be defined.");
    }

    @Test
    void okHttpClientValue() {

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .orgID("00")
                .okHttpClient(okHttpClient)
                .build();

        Assertions.assertThat(fluxConnectionOptions.getOkHttpClient()).isEqualTo(okHttpClient);
    }
}