package org.influxdb.dto;

import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.influxdb.impl.Preconditions;

import static org.influxdb.dto.Point.escapeKey;

/**
 *
 *
 * @author hoan.le [at] bonitoo.io
 *
 */
public class Series {
  private SeriesStringBuilder seriesStringBuilder = new SeriesStringBuilder();
  private final ThreadLocal<SeriesStringBuilder> cachedStringBuilders = ThreadLocal.withInitial(() -> {
    return new SeriesStringBuilder(seriesStringBuilder);
  });
  private String measurement;
  private SortedMap<String, String> tags = new TreeMap<String, String>();

  public Series(final String measurement, final Map<String, String> tags) {
    Preconditions.checkNonEmptyString(measurement, "measurement");
    this.measurement = measurement;
    for (Entry<String, String> tag : tags.entrySet()) {
      String tagName = tag.getKey();
      String value = tag.getValue();
      Objects.requireNonNull(tagName, "tagName");
      Objects.requireNonNull(value, "value");
      if (!tagName.isEmpty() && !value.isEmpty()) {
        this.tags.put(tagName, value);
      }
    }

    StringBuilder seriesLineProtocol = seriesStringBuilder.seriesLineProtocol;
    escapeKey(seriesLineProtocol, measurement);
    for (Entry<String, String> tag : this.tags.entrySet()) {
      seriesLineProtocol.append(',');
      escapeKey(seriesLineProtocol, tag.getKey());
      seriesLineProtocol.append('=');
      escapeKey(seriesLineProtocol, tag.getValue());
    }
    seriesLineProtocol.append(' ');
    seriesStringBuilder.length = seriesLineProtocol.length();
  }

  public SeriesPointBuilder createPointBuilder() {
    return new SeriesPointBuilder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Series)) {
      return false;
    }
    Series series = (Series) o;
    return Objects.equals(measurement, series.measurement) && Objects.equals(tags, series.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(measurement, tags);
  }

  public final class SeriesPointBuilder {
    private long time;
    private TimeUnit precision;
    private SortedMap<String, Object> fields = new TreeMap<>();

    private SeriesPointBuilder() {

    }

    public SeriesPointBuilder addField(final String field, final boolean value) {
      return field(field, value);
    }

    public SeriesPointBuilder addField(final String field, final long value) {
      return field(field, value);
    }

    public SeriesPointBuilder addField(final String field, final double value) {
      return field(field, value);
    }

    public SeriesPointBuilder addField(final String field, final Number value) {
      return field(field, value);
    }

    public SeriesPointBuilder addField(final String field, final String value) {
      return field(field, value);
    }

    private SeriesPointBuilder field(final String field, final Object value) {
      Objects.requireNonNull(value, "value");
      fields.put(field, value);
      return this;
    }

    /**
     * Add a Map of fields to this point.
     *
     * @param fieldsToAdd
     *          the fields to add
     * @return the Builder instance.
     */
    public SeriesPointBuilder fields(final Map<String, Object> fieldsToAdd) {
      fields.putAll(fieldsToAdd);
      return this;
    }

    public SeriesPointBuilder fields(final Supplier<Map<String, Object>> fieldSupplier) {
      fields.putAll(fieldSupplier.get());
      return this;
    }

    /**
     * Add a time to this point.
     *
     * @param timeToSet
     *          the time for this point
     * @param precisionToSet
     *          the TimeUnit
     * @return the Builder instance.
     */
    public SeriesPointBuilder time(final long timeToSet, final TimeUnit precisionToSet) {
      Objects.requireNonNull(precisionToSet, "precisionToSet");
      this.time = timeToSet;
      this.precision = precisionToSet;
      return this;
    }

    /**
     * Create a new Point.
     *
     * @return the newly created Point.
     */
    public Point build() {
      Preconditions.checkPositiveNumber(fields.size(), "fields size");
      SeriesPoint point = new SeriesPoint();
      point.setFields(fields);
      if (precision != null) {
        point.setTime(time);
        point.setPrecision(precision);
      }
      return point;
    }
  }

  private class SeriesStringBuilder {
    StringBuilder seriesLineProtocol = new StringBuilder();
    int length;

    SeriesStringBuilder(final SeriesStringBuilder seriesStringBuilder) {
      seriesLineProtocol.append(seriesStringBuilder.seriesLineProtocol);
      length = seriesStringBuilder.length;
    }

    SeriesStringBuilder() {

    }

    void reset() {
      seriesLineProtocol.setLength(length);
    }
  }

  private class SeriesPoint extends Point {
    SeriesPoint() {
      setMeasurement(measurement);
      setTags(tags);
    }

    @Override
    public String lineProtocol() {
      SeriesStringBuilder seriesStringBuilder = cachedStringBuilders.get();
      StringBuilder seriesLineProtocol = seriesStringBuilder.seriesLineProtocol;
      try {
        concatenatedFields(seriesLineProtocol);
        formatedTime(seriesLineProtocol);

        return seriesLineProtocol.toString();
      } finally {
        seriesStringBuilder.reset();
      }

    }

    @Override
    public String lineProtocol(final TimeUnit precision) {
      SeriesStringBuilder seriesStringBuilder = cachedStringBuilders.get();
      StringBuilder seriesLineProtocol = seriesStringBuilder.seriesLineProtocol;
      try {
        concatenatedFields(seriesLineProtocol);
        formatedTime(seriesLineProtocol);

        return seriesLineProtocol.toString();
      } finally {
        seriesStringBuilder.reset();
      }
    }

  }

}
