package org.influxdb.reactive;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (04/06/2018 11:48)
 */
@Measurement(name = "h2o_feet", timeUnit = TimeUnit.NANOSECONDS)
public class H2OFeetMeasurement {

    @Column(name = "location", tag = true)
    private String location;

    @Column(name = "water_level")
    private Double level;

    @Column(name = "level description")
    private String description;

    @Column(name = "time")
    private Instant time;

    public H2OFeetMeasurement() {
    }

    public H2OFeetMeasurement(String location, Double level, String description, Long time) {
        this.location = location;
        this.level = level;
        this.description = description;
        this.time = Instant.ofEpochMilli(time);
    }

    public String getLocation() {
        return location;
    }

    public Double getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof H2OFeetMeasurement)) return false;
        H2OFeetMeasurement h20Feet = (H2OFeetMeasurement) o;
        return Objects.equals(location, h20Feet.location) &&
                Objects.equals(level, h20Feet.level) &&
                Objects.equals(description, h20Feet.description) &&
                Objects.equals(time, h20Feet.time);
    }

    @Override
    public int hashCode() {

        return Objects.hash(location, level, description, time);
    }
}