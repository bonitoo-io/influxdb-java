package org.influxdb.reactive;

import io.reactivex.observers.TestObserver;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.InfluxDBException;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.influxdb.reactive.events.QueryParsedResponseEvent;
import org.influxdb.reactive.events.WriteErrorEvent;
import org.influxdb.reactive.events.WritePartialEvent;
import org.influxdb.reactive.events.WriteSuccessEvent;
import org.influxdb.reactive.events.WriteUDPEvent;
import org.influxdb.reactive.options.BatchOptionsReactive;
import org.influxdb.reactive.options.WriteOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (14/06/2018 09:27)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveEventsTest extends AbstractInfluxDBReactiveTest {

    @BeforeEach
    void setUp() {
        super.setUp(BatchOptionsReactive.builder().batchSize(1).build());
    }

    @Test
    void successWriteEvent() {

        TestObserver<WriteSuccessEvent> listener = influxDBReactive
                .listenEvents(WriteSuccessEvent.class)
                .test();

        influxDBServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement = createMeasurement();
        influxDBReactive.writeMeasurement(measurement);

        listener
                .assertValueCount(1)
                .assertValue(writeSuccessEvent -> {

                    List<H2OFeetMeasurement> dataPoints = writeSuccessEvent.getDataPoints();

                    Assertions.assertThat(dataPoints.size()).isEqualTo(1);
                    Assertions.assertThat(dataPoints.get(0)).isEqualTo(measurement);

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

        H2OFeetMeasurement measurement = createMeasurement();
        influxDBReactive.writeMeasurement(measurement);

        listener
                .assertValueCount(1)
                .assertValue(writeErrorEvent -> {

                    List<H2OFeetMeasurement> dataPoints = writeErrorEvent.getDataPoints();

                    Assertions.assertThat(dataPoints.size()).isEqualTo(1);
                    Assertions.assertThat(dataPoints.get(0)).isEqualTo(measurement);

                    Assertions.assertThat(writeErrorEvent.getException().isRetryWorth()).isEqualTo(false);
                    Assertions.assertThat(writeErrorEvent.getException().getMessage())
                            .isEqualTo("database not found: not_exist_database");

                    WriteOptions expectedOptions = WriteOptions.builder().database("weather").build();
                    Assertions.assertThat(writeErrorEvent.getWriteOptions()).isEqualTo(expectedOptions);

                    return true;
                });
    }

    @Test
    void writePartialEvent() {

        TestObserver<WritePartialEvent> listener = influxDBReactive
                .listenEvents(WritePartialEvent.class)
                .test();

        TestObserver<WriteErrorEvent> listenerError = influxDBReactive
                .listenEvents(WriteErrorEvent.class)
                .test();

        TestObserver<WriteSuccessEvent> listenerSuccess = influxDBReactive
                .listenEvents(WriteSuccessEvent.class)
                .test();

        // Partial response
        String influxDBError = "partial write: unable to parse 'cpu_load_short,host=server02,region=us-west "
                + "value=0.55x 1422568543702900257': invalid number unable to parse "
                + "'cpu_load_short,direction=in,host=server01,region=us-west 1422568543702900257': "
                + "invalid field format dropped=0";

        influxDBServer.enqueue(createErrorResponse(influxDBError));

        H2OFeetMeasurement measurement = createMeasurement();
        influxDBReactive.writeMeasurement(measurement);

        listenerError.assertValueCount(0);
        listenerSuccess.assertValueCount(0);

        listener.assertValueCount(1).assertValue(event -> {

            List<H2OFeetMeasurement> dataPoints = event.getDataPoints();

            Assertions.assertThat(dataPoints.size()).isEqualTo(1);
            Assertions.assertThat(dataPoints.get(0)).isEqualTo(measurement);

            WriteOptions expectedOptions = WriteOptions.builder().database("weather").build();
            Assertions.assertThat(event.getWriteOptions()).isEqualTo(expectedOptions);

            Assertions.assertThat(event.getException()).isNotNull();
            Assertions.assertThat(event.getException()).isInstanceOf(InfluxDBException.PartialWriteException.class);
            Assertions.assertThat(influxDBError).isEqualTo(event.getException().getMessage());

            return true;
        });
    }

    @Test
    void writeUDPEvent() {

        TestObserver<WriteUDPEvent> listener = influxDBReactive.listenEvents(WriteUDPEvent.class).test();

        H2OFeetMeasurement measurement = createMeasurement();
        WriteOptions writeOptions = WriteOptions.builder().udp(true, 8089).build();

        influxDBReactive.writeMeasurement(measurement, writeOptions);

        listener.assertValueCount(1)
                .assertValue(event -> {

                    List<H2OFeetMeasurement> dataPoints = event.getDataPoints();

                    Assertions.assertThat(dataPoints.size()).isEqualTo(1);
                    Assertions.assertThat(dataPoints.get(0)).isEqualTo(measurement);

                    Assertions.assertThat(event.getWriteOptions()).isEqualTo(writeOptions);

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
}
