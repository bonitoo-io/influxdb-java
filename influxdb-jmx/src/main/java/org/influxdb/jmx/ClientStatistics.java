package org.influxdb.jmx;

import java.util.concurrent.atomic.AtomicLong;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBOptions;

/**
 * Implementation of InfluxDB java client statistics MXBean.
 * Instance is created in {@link JmxMonitorEventListener} and
 * <p>
 * {@link InfluxDB#close()} must be called to unregister {@link ClientStatistics} from JMX Server resource.
 */
class ClientStatistics implements ClientStatisticsMBean {

    private final AtomicLong writeCount = new AtomicLong();
    private final AtomicLong queryCount = new AtomicLong();

    private final AtomicLong errorRequestsCount = new AtomicLong();
    private final AtomicLong successRequestsCount = new AtomicLong();
    private final OkHttpClient okHttpClient;
    private final InfluxDBOptions influxDBOptions;

    ClientStatistics(final OkHttpClient okHttpClient, final InfluxDBOptions influxDBOptions) {
        this.okHttpClient = okHttpClient;
        this.influxDBOptions = influxDBOptions;
    }

    @Override
    public void reset() {
        writeCount.set(0);
        queryCount.set(0);
        errorRequestsCount.set(0);
        successRequestsCount.set(0);
    }

    void incWriteCount() {
        writeCount.incrementAndGet();
    }

    @Override
    public int getConnectionCount() {
        return okHttpClient.connectionPool().connectionCount();
    }

    @Override
    public int getBusyConnectionCount() {
        return getConnectionCount() - getIdleConnectionCount();
    }

    @Override
    public int getIdleConnectionCount() {
        return okHttpClient.connectionPool().idleConnectionCount();
    }

    void incSuccessCount() {
        successRequestsCount.incrementAndGet();
    }

    void incErrorCount() {
        errorRequestsCount.incrementAndGet();
    }

    public long getWriteCount() {
        return writeCount.longValue();
    }

    @Override
    public String getHostAddress() {
        return influxDBOptions.getUrl();
    }

    @Override
    public long getErrorCount() {
        return errorRequestsCount.longValue();
    }

    @Override
    public long getSuccessCount() {
        return successRequestsCount.longValue();
    }

    void incQueryCount() {
        queryCount.incrementAndGet();
    }

    @Override
    public long getQueryCount() {
        return queryCount.longValue();

    }
}
