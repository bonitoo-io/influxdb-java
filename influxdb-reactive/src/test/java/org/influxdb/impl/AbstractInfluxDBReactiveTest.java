package org.influxdb.impl;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.influxdb.InfluxDBOptions;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.junit.jupiter.api.AfterEach;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 07:04)
 */
public abstract class AbstractInfluxDBReactiveTest {

    protected InfluxDBReactive influxDBReactive;
    protected InfluxDBReactiveListenerVerifier verifier;

    protected TestScheduler batchScheduler;
    protected TestScheduler jitterScheduler;

    protected MockWebServer influxDBServer;

    protected void setUp(@Nonnull final BatchOptionsReactive batchOptions) {

        Objects.requireNonNull(batchOptions, "BatchOptionsReactive is required");

        influxDBServer = new MockWebServer();
        try {
            influxDBServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url(influxDBServer.url("/").url().toString())
                .username("admin")
                .password("password")
                .database("weather")
                .build();

        verifier = new InfluxDBReactiveListenerVerifier();
        batchScheduler = new TestScheduler();
        jitterScheduler = new TestScheduler();

        influxDBReactive = new InfluxDBReactiveImpl(options, batchOptions,
                Schedulers.trampoline(), batchScheduler, jitterScheduler,
                null, verifier);

    }

    @AfterEach
    void cleanUp() throws IOException {
        influxDBReactive.close();
        influxDBServer.shutdown();
    }

    @Nonnull
    protected String pointsBody() {

        Buffer sink;
        try {
            sink = influxDBServer.takeRequest().getBody();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return sink.readUtf8();
    }
}
