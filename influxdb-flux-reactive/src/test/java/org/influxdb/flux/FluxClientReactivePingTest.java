package org.influxdb.flux;

import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractFluxClientReactiveTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/07/2018 09:52)
 */
@RunWith(JUnitPlatform.class)
class FluxClientReactivePingTest extends AbstractFluxClientReactiveTest {

    @Test
    void serverError() {

        fluxServer.enqueue(createErrorResponse(""));

        fluxClient
                .ping()
                .test()
                .assertValueCount(1)
                .assertValue(running -> {

                    Assertions.assertThat(running).isFalse();

                    return true;
                });

    }

    @Test
    void healthy() {

        fluxServer.enqueue(new MockResponse().setResponseCode(204));

        fluxClient
                .ping()
                .test()
                .assertValueCount(1)
                .assertValue(running -> {

                    Assertions.assertThat(running).isTrue();

                    return true;
                });
    }
}