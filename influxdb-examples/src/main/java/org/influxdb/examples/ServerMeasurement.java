package org.influxdb.examples;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "host")
public class ServerMeasurement {

    @Column(tag = true, name = "hostname")
    private String hostname;
    @Column(name = "systemLoadAverage")
    private double systemLoadAverage;
    @Column(name = "processCpuLoad")
    private double processCpuLoad;
    @Column(name = "freePhysicalMemorySize")
    private long freePhysicalMemorySize;

    @Override
    public String toString() {
        return "ServerMeasurement{" + "hostname='" + hostname + '\'' + ", systemLoadAverage=" + systemLoadAverage +
                ", processCpuLoad=" + processCpuLoad + ", freePhysicalMemorySize=" + freePhysicalMemorySize + '}';
    }

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

    public static ServerMeasurement getCurrentMeasurement() {
        ServerMeasurement m = new ServerMeasurement();
        m.setHostname(Utils.getHostName());
        m.setSystemLoadAverage(Utils.getOperatingSystemMBean().getSystemLoadAverage());
        m.setProcessCpuLoad(Utils.getOperatingSystemMBean().getProcessCpuLoad());
        m.setFreePhysicalMemorySize(Utils.getOperatingSystemMBean().getFreePhysicalMemorySize());
        return m;
    }

}
