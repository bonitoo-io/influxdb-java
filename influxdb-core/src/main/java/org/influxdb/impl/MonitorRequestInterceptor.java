package org.influxdb.impl;

import java.io.IOException;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.influxdb.InfluxDBEventListener;

/**
 * OkHttpClient interceptor that count success and error calls.
 * @since 3.0
 */
public class MonitorRequestInterceptor implements Interceptor {

    private List<InfluxDBEventListener> influxDBEventListeners;

    public MonitorRequestInterceptor(final List<InfluxDBEventListener> influxDBEventListeners) {
        this.influxDBEventListeners = influxDBEventListeners;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (response.isSuccessful()) {
            influxDBEventListeners.forEach(l -> l.onSuccess(request, response));
        } else {
            influxDBEventListeners.forEach(l -> l.onError(request, response));
        }
        return response;

    }
}
