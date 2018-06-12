package org.influxdb.examples;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "host", timeUnit = TimeUnit.NANOSECONDS)
public class ServerMeasurement {

    @Column(name = "time")
    private Instant time;

    @Column(tag = true, name = "hostname")
    private String hostname;

    @Column(name = "systemLoadAverage")
    private double systemLoadAverage;

    @Column(name = "processCpuLoad")
    private double processCpuLoad;

    @Column(name = "freePhysicalMemorySize")
    private long freePhysicalMemorySize;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public static ServerMeasurement getCurrentMeasurement() {
        ServerMeasurement m = new ServerMeasurement();
        try {
            m.setHostname(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ignore) {
            //
        }
        m.setSystemLoadAverage(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        m.setProcessCpuLoad(getOSMBean().getProcessCpuLoad());
        m.setFreePhysicalMemorySize(getOSMBean().getFreePhysicalMemorySize());
        m.setTime(Instant.now());
        return m;
    }

    @Override
    public String toString() {
        return "ServerMeasurement{time='" + time + "', " + "hostname='"
                + hostname + '\'' + ", systemLoadAverage=" + systemLoadAverage
                + ", processCpuLoad=" + processCpuLoad + ", freePhysicalMemorySize="
                + freePhysicalMemorySize + '}';
    }

    private static com.sun.management.OperatingSystemMXBean getOSMBean() {

        return (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    }


}
