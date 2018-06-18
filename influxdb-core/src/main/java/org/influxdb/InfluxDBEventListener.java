package org.influxdb;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Listener for InfluxDB client related events.
 * @since 3.0
 */
public interface InfluxDBEventListener {

    /**
     * Invoked when a client is created.
     */
    void onCreate(OkHttpClient okHttpClient, InfluxDBOptions influxDBOptions);

    /**
     * Invoked on {@link InfluxDB#close()}.
     */
    void onDestroy();

    /**
     * Invoked on error response from server.
     */
    void onError(Request request, Response response);

    /**
     * Invoked on success.
     */
    void onSuccess(Request request, Response response);
}
