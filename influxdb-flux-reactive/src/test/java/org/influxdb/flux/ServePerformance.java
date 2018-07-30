package org.influxdb.flux;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 07:31)
 */
@Measurement(name = "server_performance")
public class ServePerformance {

    @Column(name = "location", tag = true)
    String location;

    @Column(name = "cpu_usage")
    Double cpuUsage;

    @Column(name = "server description")
    String description;

    @Column(name = "upTime")
    Long upTime;

    @Column(name = "rackNumber")
    Integer rackNumber;

    @Column(name = "production_usage", tag = true)
    boolean production;

    @Column(name = "time")
    private Instant time;

    public ServePerformance() {
    }

    @Nonnull
    static ServePerformance create(@Nonnull final Integer index) {
        Objects.requireNonNull(index, "Measurement index is required");

        ServePerformance servePerformance = new ServePerformance();
        servePerformance.time = Instant.ofEpochMilli(1_530_079_000_000L + index);
        servePerformance.production = ((index % 2) == 0);
        servePerformance.rackNumber = index;
        servePerformance.upTime = Long.valueOf(index) * 10_000;
        servePerformance.description = "Server no. " + index;
        servePerformance.cpuUsage = 50 * index.doubleValue();
        servePerformance.location = String.format("Area %s° %s' \"%s", index, index * 10, index * 20);

        return servePerformance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServePerformance)) return false;
        ServePerformance that = (ServePerformance) o;
        return production == that.production &&
                Objects.equals(location, that.location) &&
                Objects.equals(cpuUsage, that.cpuUsage) &&
                Objects.equals(description, that.description) &&
                Objects.equals(upTime, that.upTime) &&
                Objects.equals(rackNumber, that.rackNumber) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {

        return Objects.hash(location, cpuUsage, description, upTime, rackNumber, production, time);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("org.influxdb.flux.ServePerformance{");
        sb.append("location='").append(location).append('\'');
        sb.append(", cpuUsage=").append(cpuUsage);
        sb.append(", description='").append(description).append('\'');
        sb.append(", upTime=").append(upTime);
        sb.append(", rackNumber=").append(rackNumber);
        sb.append(", production=").append(production);
        sb.append(", time=").append(time);
        sb.append('}');
        return sb.toString();
    }
}