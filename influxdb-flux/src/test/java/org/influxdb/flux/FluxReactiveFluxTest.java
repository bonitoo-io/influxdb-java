package org.influxdb.flux;

import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.InfluxDBException;
import org.influxdb.flux.events.FluxErrorEvent;
import org.influxdb.flux.events.FluxSuccessEvent;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.impl.AbstractFluxReactiveTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:54)
 */
@RunWith(JUnitPlatform.class)
class FluxReactiveFluxTest extends AbstractFluxReactiveTest {

    @Test
    void successFluxResponseEvent() {

        fluxServer.enqueue(createResponse());

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

    @Test
    void errorFluxResponseEvent() {

        String influxDBError = "must pass organization name or ID as string in orgName or orgID parameter";
        fluxServer.enqueue(createErrorResponse(influxDBError));

        TestObserver<FluxErrorEvent> listener = fluxReactive
                .listenEvents(FluxErrorEvent.class)
                .test();

        Flowable<FluxResult> results = fluxReactive.flux(Flux.from("flux_database"));
        results
                .take(1)
                .test()
                .assertError(InfluxDBException.class)
                .assertErrorMessage(influxDBError);

        listener
                .assertValueCount(1)
                .assertValue(event -> {

                    Assertions.assertThat(event.getFluxQuery()).isEqualTo("from(db:\"flux_database\")");
                    Assertions.assertThat(event.getOptions()).isNotNull();
                    Assertions.assertThat(event.getException()).isInstanceOf(InfluxDBException.class);
                    Assertions.assertThat(event.getException()).hasMessage(influxDBError);

                    return true;
                });
    }

    @Test
    void parsingToFluxResult() {

        fluxServer.enqueue(createResponse());

        Flowable<FluxResult> results = fluxReactive.flux(Flux.from("flux_database"));
        results
                .take(1)
                .test()
                .assertValueCount(1)
                .assertValue(fluxResult -> {

                    Assertions.assertThat(fluxResult).isNotNull();
                    return true;
                });
    }

    @Nonnull
    private MockResponse createResponse() {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,string,string,double\n"
                + ",result,table,_start,_stop,_time,region,host,_value\n"
                + ",mean,0,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:00Z,east,A,15.43\n"
                + ",mean,0,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:20Z,east,B,59.25\n"
                + ",mean,0,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:40Z,east,C,52.62\n"
                + ",mean,1,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:00Z,west,A,62.73\n"
                + ",mean,1,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:20Z,west,B,12.83\n"
                + ",mean,1,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:40Z,west,C,51.62";

        return new MockResponse()
                .setHeader("Content-Type", "text/csv; charset=utf-8")
                .setHeader("Date", "Tue, 26 Jun 2018 13:15:01 GMT")
                .setChunkedBody(data, data.length());
    }

    @Nonnull
    private MockResponse createErrorResponse(@Nullable final String influxDBError) {

        String body = String.format("{\"error\":\"%s\"}", influxDBError);

        return new MockResponse()
                .setResponseCode(500)
                .addHeader("X-Influxdb-Error", influxDBError)
                .setBody(body);
    }
}