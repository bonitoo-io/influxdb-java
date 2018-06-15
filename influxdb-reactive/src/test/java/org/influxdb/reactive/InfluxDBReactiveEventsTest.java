package org.influxdb.reactive;

import io.reactivex.observers.TestObserver;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.influxdb.reactive.event.QueryParsedResponseEvent;
import org.influxdb.reactive.event.WriteErrorEvent;
import org.influxdb.reactive.event.WriteSuccessEvent;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.influxdb.reactive.option.WriteOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (14/06/2018 09:27)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveEventsTest extends AbstractInfluxDBReactiveTest {

    @BeforeEach
    void setUp() {
        super.setUp(BatchOptionsReactive.DISABLED);
    }

    @Test
    void successWriteEvent() {

        TestObserver<WriteSuccessEvent> listener = influxDBReactive
                .listenEvents(WriteSuccessEvent.class)
                .test();

        influxDBServer.enqueue(new MockResponse());

        influxDBReactive.writeMeasurement(createMeasurement());

        listener
                .assertValueCount(1)
                .assertValue(writeSuccessEvent -> {

                    Assertions.assertThat(writeSuccessEvent.getPoints().size()).isEqualTo(1);
                    Assertions.assertThat(writeSuccessEvent.getPoints().get(0)).isEqualTo(createMeasurementPoint());

                    WriteOptions expectedOptions = WriteOptions.builder().database("weather").build();
                    Assertions.assertThat(writeSuccessEvent.getWriteOptions()).isEqualTo(expectedOptions);

                    return true;
                });
    }

    @Test
    void writeErrorEvent() {

        TestObserver<WriteErrorEvent> listener = influxDBReactive
                .listenEvents(WriteErrorEvent.class)
                .test();

        // Only error Retry Error than Success
        influxDBServer.enqueue(createErrorResponse("database not found: not_exist_database"));

        influxDBReactive.writeMeasurement(createMeasurement());

        listener
                .assertValueCount(1)
                .assertValue(writeErrorEvent -> {

                    Assertions.assertThat(writeErrorEvent.getPoints().size()).isEqualTo(1);
                    Assertions.assertThat(writeErrorEvent.getPoints().get(0)).isEqualTo(createMeasurementPoint());

                    Assertions.assertThat(writeErrorEvent.getException().isRetryWorth()).isEqualTo(false);
                    Assertions.assertThat(writeErrorEvent.getException().getMessage())
                            .isEqualTo("database not found: not_exist_database");

                    WriteOptions expectedOptions = WriteOptions.builder().database("weather").build();
                    Assertions.assertThat(writeErrorEvent.getWriteOptions()).isEqualTo(expectedOptions);

                    return true;
                });
    }

    @Test
    void queryParsedResponseEvent() {

        TestObserver<QueryParsedResponseEvent> listener = influxDBReactive
                .listenEvents(QueryParsedResponseEvent.class)
                .test();

        Query query = new Query("select * from not_exist group by *", "reactive_database");
        String body = "{\"results\":[{\"statement_id\":0}]}";

        influxDBServer.enqueue(new MockResponse().setBody(body));

        influxDBReactive.query(query).test().assertValueCount(1);

        listener
                .assertValueCount(1)
                .assertValue(queryParsedResponseEvent -> {

                    Assertions.assertThat(queryParsedResponseEvent.getQueryResult()).isNotNull();
                    Assertions.assertThat(queryParsedResponseEvent.getBufferedSource()).isNotNull();

                    QueryResult queryResult = queryParsedResponseEvent.getQueryResult();
                    Assertions.assertThat(queryResult.getError()).isNull();
                    Assertions.assertThat(queryResult.getResults().size()).isEqualTo(1);
                    Assertions.assertThat(queryResult.getResults().get(0).getError()).isNull();
                    Assertions.assertThat(queryResult.getResults().get(0).getSeries()).isNull();

                    return true;
                });

    }

    @Nonnull
    private H2OFeetMeasurement createMeasurement() {
        return new H2OFeetMeasurement("coyote_creek", 2.927, "below 3 feet", 1440046800L);
    }

    @Nonnull
    private Point createMeasurementPoint() {
        return Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level description", "below 3 feet")
                .time(1440046800L, TimeUnit.NANOSECONDS)
                .build();
    }
}
