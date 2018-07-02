package org.influxdb.flux.mapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the record in CSV response.
 */
public class Record {

    private Instant start;
    private Instant stop;
    private Instant time;
    private Object value;

    private String field;
    private String measurement;
    private Map<String, String> tags = new HashMap<>();

    public Instant getStart() {
        return start;
    }

    public void setStart(final Instant start) {
        this.start = start;
    }

    public Instant getStop() {
        return stop;
    }

    public void setStop(final Instant stop) {
        this.stop = stop;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(final Instant time) {
        this.time = time;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(final String measurement) {
        this.measurement = measurement;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(final Map<String, String> tags) {
        this.tags = tags;
    }
}
