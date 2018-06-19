package org.influxdb.impl;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBEventListener;
import org.influxdb.InfluxDBIOException;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Pong;
import org.influxdb.dto.QueryResult;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (04/06/2018 09:45)
 */
public abstract class AbstractInfluxDB<T> {

    private final OkHttpClient okHttpClient;
    private final InetAddress hostAddress;
    private volatile DatagramSocket datagramSocket;

    final T influxDBService;

    final HttpLoggingInterceptor loggingInterceptor;

    final GzipRequestInterceptor gzipRequestInterceptor;

    InfluxDB.LogLevel logLevel = InfluxDB.LogLevel.NONE;
    JsonAdapter<QueryResult> adapter;
    private List<InfluxDBEventListener> listeners;


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

        this.hostAddress = parseHostAddress(options.getUrl());
        this.okHttpClient = okBuilder.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create());

        configure(builder);

        if (service == null) {
            Retrofit retrofit = builder.build();
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
    Pong createPong(final long requestStart, @Nonnull final Response<ResponseBody> response) {

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

    void destroy() {

        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }

        listeners.forEach(InfluxDBEventListener::onDestroy);
    }

    void writeRecordsThroughUDP(final int udpPort, @Nullable final String records) {

        if (records == null) {
            return;
        }

        initialDatagramSocket();

        byte[] bytes = records.getBytes(StandardCharsets.UTF_8);
        try {
            datagramSocket.send(new DatagramPacket(bytes, bytes.length, hostAddress, udpPort));
        } catch (IOException e) {
            throw new InfluxDBIOException(e);
        }
    }

    @Nonnull
    private InetAddress parseHostAddress(@Nonnull final String url) {

        Objects.requireNonNull(url, "URL is required");

        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            throw new IllegalArgumentException("Unable to parse url: " + url);
        }

        try {
            return InetAddress.getByName(httpUrl.host());
        } catch (UnknownHostException e) {
            throw new InfluxDBIOException(e);
        }
    }

    private void initialDatagramSocket() {
        if (datagramSocket == null) {
            synchronized (InfluxDBImpl.class) {
                if (datagramSocket == null) {
                    try {
                        datagramSocket = new DatagramSocket();
                    } catch (SocketException e) {
                        throw new InfluxDBIOException(e);
                    }
                }
            }
        }
    }
}
