package org.influxdb.impl;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.logging.HttpLoggingInterceptor;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.QueryResult;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (04/06/2018 09:45)
 */
public abstract class AbstractInfluxDB<T> {

    final T influxDBService;

    final HttpLoggingInterceptor loggingInterceptor;

    final GzipRequestInterceptor gzipRequestInterceptor;

    InfluxDB.LogLevel logLevel = InfluxDB.LogLevel.NONE;
    JsonAdapter<QueryResult> adapter;

    AbstractInfluxDB(@Nonnull final Class<T> influxDBServiceClass,
                     @Nonnull final InfluxDBOptions options,
                     @Nullable final T service,
                     @Nullable final JsonAdapter<QueryResult> adapter) {

        Objects.requireNonNull(options, "InfluxDBOptions is required");

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.gzipRequestInterceptor = new GzipRequestInterceptor();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(options.getOkHttpClient()
                        .addInterceptor(loggingInterceptor)
                        .addInterceptor(gzipRequestInterceptor).build())
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

    }

    /**
     * @param builder to configure Retrofit
     */
    protected void configure(@Nonnull final Retrofit.Builder builder) {
    }
}
