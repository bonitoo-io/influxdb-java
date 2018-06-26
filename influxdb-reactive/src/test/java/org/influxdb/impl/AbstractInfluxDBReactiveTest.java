package org.influxdb.impl;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.influxdb.InfluxDBOptions;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.junit.jupiter.api.AfterEach;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 07:04)
 */
public abstract class AbstractInfluxDBReactiveTest {

    protected InfluxDBReactive influxDBReactive;
    protected InfluxDBReactiveVerifier verifier;

    protected Scheduler batchScheduler;
    protected Scheduler jitterScheduler;
    protected Scheduler retryScheduler;

    protected MockWebServer influxDBServer;

    protected void setUp(@Nonnull final BatchOptionsReactive batchOptions) {
        setUp(batchOptions, new TestScheduler(), new TestScheduler(), new TestScheduler());
    }

    protected void setUp(@Nonnull final BatchOptionsReactive batchOptions,
                         @Nonnull final Scheduler batchScheduler,
                         @Nonnull final Scheduler jitterScheduler,
                         @Nonnull final Scheduler retryScheduler) {

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

        this.batchScheduler = batchScheduler;
        this.jitterScheduler = jitterScheduler;
        this.retryScheduler = retryScheduler;

        influxDBReactive = new InfluxDBReactiveImpl(options, batchOptions,
                Schedulers.trampoline(), this.batchScheduler, this.jitterScheduler,
                this.retryScheduler, null);

        verifier = new InfluxDBReactiveVerifier(influxDBReactive);

    }

    protected void advanceTimeBy(int i, @Nonnull final Scheduler scheduler) {
        ((TestScheduler) scheduler).advanceTimeBy(i, TimeUnit.SECONDS);
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

    @Nonnull
    protected MockResponse createErrorResponse(@Nullable final String influxDBError) {

        String body = String.format("{\"error\":\"%s\"}", influxDBError);

        return new MockResponse()
                .setResponseCode(400)
                .addHeader("X-Influxdb-Error", influxDBError)
                .setBody(body);
    }
}
