package org.influxdb.flux;

import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractFluxClientTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:19)
 */
@RunWith(JUnitPlatform.class)
class FluxClientPingTest extends AbstractFluxClientTest {

    @Test
    void serverError() {

        fluxServer.enqueue(createErrorResponse(""));

        Assertions.assertThat(fluxClient.ping()).isFalse();
    }

    @Test
    void healthy() {

        fluxServer.enqueue(new MockResponse().setResponseCode(204));

        Assertions.assertThat(fluxClient.ping()).isTrue();
    }
}