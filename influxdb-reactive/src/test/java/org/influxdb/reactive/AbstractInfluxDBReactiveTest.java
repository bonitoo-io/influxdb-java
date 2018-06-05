package org.influxdb.reactive;

import io.reactivex.Single;
import okhttp3.RequestBody;
import okio.Buffer;
import org.influxdb.InfluxDBOptions;
import org.influxdb.impl.InfluxDBReactiveImpl;
import org.influxdb.impl.InfluxDBServiceReactive;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 07:04)
 */
abstract class AbstractInfluxDBReactiveTest {

    InfluxDBReactive influxDB;
    InfluxDBServiceReactive influxDBService;

    ArgumentCaptor<RequestBody> requestBody;

    void setUp(@Nonnull final BatchOptionsReactive batchOptions) {

        Objects.requireNonNull(batchOptions, "BatchOptionsReactive is required");

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://influxdb:8086")
                .username("admin")
                .password("password")
                .database("weather")
                .build();

        influxDBService = Mockito.mock(InfluxDBServiceReactive.class);
        influxDB = new InfluxDBReactiveImpl(options, batchOptions, influxDBService);

        requestBody = ArgumentCaptor.forClass(RequestBody.class);

        Mockito.doAnswer(invocation -> Single.just(Response.success(null))).when(influxDBService).writePoints(
                Mockito.eq("admin"),
                Mockito.eq("password"),
                Mockito.eq("weather"),
                Mockito.eq("autogen"),
                Mockito.any(),
                Mockito.eq("one"),
                requestBody.capture());
    }

    @AfterEach
    void cleanUp() {
        influxDB.close();
    }

    @Nonnull
    String pointsBody() {
        return pointsBody(0);
    }

    @Nonnull
    String pointsBody(@Nonnull final Integer captureValueIndex) {

        Objects.requireNonNull(captureValueIndex, "CaptureValueIndex is required");

        Buffer sink = new Buffer();
        try {
            requestBody.getAllValues().get(captureValueIndex).writeTo(sink);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sink.readUtf8();
    }
}