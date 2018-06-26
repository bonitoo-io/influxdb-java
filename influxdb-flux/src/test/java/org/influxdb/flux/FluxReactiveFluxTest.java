package org.influxdb.flux;

import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.flux.events.FluxSuccessEvent;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.impl.AbstractFluxReactiveTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:54)
 */
@RunWith(JUnitPlatform.class)
class FluxReactiveFluxTest extends AbstractFluxReactiveTest {

    @Test
    void successFluxResponseEvent() {

        String body = "{\"results\":[{\"statement_id\":0,\"series\":[{\"name\":\"h2o_feet\"," +
                "\"tags\":{\"location\":\"coyote_creek\"},\"columns\":[\"time\",\"level description\",\"water_level\"],"
                + "\"values\":[[\"1970-01-01T00:00:00.001Z\",\"below 3 feet\",2.927]]}]}]}";

        fluxServer.enqueue(new MockResponse().setBody(body));

        TestObserver<FluxSuccessEvent> listener = fluxReactive
                .listenEvents(FluxSuccessEvent.class)
                .test();

        Flowable<FluxResult> results = fluxReactive.flux(Flux.from("flux_database"));
        results
                .take(1)
                .test()
                .assertValueCount(1);

        listener.assertValueCount(1).assertValue(event -> {

            Assertions.assertThat(event.getFluxQuery()).isEqualTo("from(db:\"flux_database\")");
            Assertions.assertThat(event.getOptions()).isNotNull();

            return true;
        });
    }
}