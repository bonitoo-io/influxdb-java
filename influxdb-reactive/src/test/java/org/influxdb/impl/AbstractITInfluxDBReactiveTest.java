package org.influxdb.impl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.influxdb.InfluxDBEventListener;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.reactive.H2OFeetMeasurement;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.options.BatchOptionsReactive;
import org.junit.jupiter.api.AfterEach;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 09:14)
 */
public abstract class AbstractITInfluxDBReactiveTest implements InfluxDBEventListener {

    private static final Logger LOG = Logger.getLogger(AbstractITInfluxDBReactiveTest.class.getName());

    protected static final String DATABASE_NAME = "reactive_database";

    protected InfluxDBReactive influxDBReactive;
    protected InfluxDBReactiveVerifier verifier;
    protected OkHttpClient okHttpClient;

    protected void setUp(@Nonnull final BatchOptionsReactive batchOptions) {

        Objects.requireNonNull(batchOptions, "BatchOptionsReactive is required");

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://" + influxdbIP + ":" + influxdbPort)
                .username("admin")
                .password("admin")
                .database(DATABASE_NAME)
                .precision(TimeUnit.NANOSECONDS)
                .addListener(this)
                .build();


        influxDBReactive = new InfluxDBReactiveImpl(options, batchOptions);
        verifier = new InfluxDBReactiveVerifier(influxDBReactive);

        simpleQuery("CREATE DATABASE " + DATABASE_NAME);

        verifier.reset();
    }

    @AfterEach
    void cleanUp() {
        simpleQuery("DROP DATABASE " + DATABASE_NAME);

        influxDBReactive.close();
    }

    @Override
    public void onCreate(OkHttpClient okHttpClient, InfluxDBOptions influxDBOptions) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onError(Request request, Response response) {

    }

    @Override
    public void onSuccess(Request request, Response response) {

    }

    protected void simpleQuery(@Nonnull final String simpleQuery) {

        Objects.requireNonNull(simpleQuery, "SimpleQuery is required");
        QueryResult result = influxDBReactive.query(new Query(simpleQuery, null)).blockingSingle();

        LOG.log(Level.FINEST, "Simple query: {0} result: {1}", new Object[]{simpleQuery, result});
    }

    @Nonnull
    protected List<H2OFeetMeasurement> getMeasurements() {
        return getMeasurements(DATABASE_NAME);
    }

    @Nonnull
    protected List<H2OFeetMeasurement> getMeasurements(@Nonnull  final String databaseName) {

        Objects.requireNonNull(databaseName, "Database name is required");

        Query reactive_database = new Query("select * from h2o_feet group by *", databaseName);

        return influxDBReactive.query(reactive_database, H2OFeetMeasurement.class).toList().blockingGet();
    }
}
