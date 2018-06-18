package org.influxdb.impl;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBEventListener;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Pong;
import org.influxdb.dto.QueryResult;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * @author Jakub Bednar (bednar@github) (04/06/2018 09:45)
 */
public abstract class AbstractInfluxDB<T> {

    final T influxDBService;

    final HttpLoggingInterceptor loggingInterceptor;

    final GzipRequestInterceptor gzipRequestInterceptor;

    private OkHttpClient okHttpClient;
    InfluxDB.LogLevel logLevel = InfluxDB.LogLevel.NONE;
    JsonAdapter<QueryResult> adapter;
    List<InfluxDBEventListener> listeners;


    AbstractInfluxDB(@Nonnull final Class<T> influxDBServiceClass,
                     @Nonnull final InfluxDBOptions options,
                     @Nullable final T service,
                     @Nullable final JsonAdapter<QueryResult> adapter) {

        Objects.requireNonNull(options, "InfluxDBOptions is required");

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.gzipRequestInterceptor = new GzipRequestInterceptor();
        this.listeners = options.getListeners();

        OkHttpClient.Builder okBuilder = options.getOkHttpClient()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(gzipRequestInterceptor);

        if (!this.listeners.isEmpty()) {
            okBuilder.addInterceptor(new MonitorRequestInterceptor(this.listeners));
        }

        this.okHttpClient = okBuilder.build();


        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create());

        configure(builder);

        Retrofit retrofit = builder.build();


        if (service == null) {
            this.influxDBService = retrofit.create(influxDBServiceClass);
        } else {
            this.influxDBService = service;
        }

        if (adapter == null) {
            Moshi moshi = new Moshi.Builder().build();
            this.adapter = moshi.adapter(QueryResult.class);
        } else {
            this.adapter = adapter;
        }

        //register listeners
        listeners.forEach(l -> l.onCreate(okHttpClient, options));

    }

    /**
     * @param builder to configure Retrofit
     */
    protected void configure(@Nonnull final Retrofit.Builder builder) {
    }

    @Nonnull
    protected Pong createPong(final long requestStart, @Nonnull final Response<ResponseBody> response) {

        Objects.requireNonNull(response, "Response is required");

        Headers headers = response.headers();
        String version = "unknown";
        for (String name : headers.toMultimap().keySet()) {
            if ("X-Influxdb-Version".equalsIgnoreCase(name)) {
                version = headers.get(name);
                break;
            }
        }

        Pong pong = new Pong();
        pong.setVersion(version);
        pong.setResponseTime(System.currentTimeMillis() - requestStart);

        return pong;
    }

    protected void destroy() {

        listeners.forEach(InfluxDBEventListener::onDestroy);

    }
}
