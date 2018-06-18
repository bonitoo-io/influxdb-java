package org.influxdb.jmx;

import javax.annotation.Nonnull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.influxdb.InfluxDBEventListener;
import org.influxdb.InfluxDBOptions;

public class JmxMonitorEventListener implements InfluxDBEventListener {

    private String mBeanNameParam;
    private static final String MBEAN_NAME = "org.influxdb:type=client,name=";
    private static final String DEFAULT_MBEAN_NAME_PARAM = "default";

    public JmxMonitorEventListener() {
        this(DEFAULT_MBEAN_NAME_PARAM);
    }

    /**
     * Creates a new instance of listener.
     *
     * @param name additional name used for creating JMX mBean.
     */
    public JmxMonitorEventListener(@Nonnull final String name) {
        this.mBeanNameParam = name;
    }

    private ClientStatistics stats;
    private String mBeanName;

    @Override
    public void onCreate(final OkHttpClient okHttpClient, final InfluxDBOptions influxDBOptions) {

        MBeanServer mBeanServer = MBeanServer.getMBeanServer();

        stats = new ClientStatistics(okHttpClient, influxDBOptions);
        mBeanName = getMBeanName(mBeanNameParam);
        mBeanServer.registerMBean(stats, ClientStatisticsMBean.class, mBeanName);
    }


    @Override
    public void onDestroy() {
        MBeanServer mBeanServer = MBeanServer.getMBeanServer();
        mBeanServer.unregisterMBean(mBeanName);
    }

    @Override
    public void onError(final Request request, final Response response) {
        stats.incErrorCount();
    }

    private String getFirstPath(final Request request) {

        return request.url().pathSegments().stream().findFirst().orElse(null);

    }

    @Override
    public void onSuccess(final Request request, final Response response) {

        String firstPathSegment = getFirstPath(request);

        if (firstPathSegment.startsWith("query")) {
            stats.incQueryCount();
        } else if (firstPathSegment.startsWith("write")) {
            stats.incWriteCount();
        }

        stats.incSuccessCount();
    }

    public static String getMBeanName(final String name) {
        return MBEAN_NAME + name;
    }
}
