package org.influxdb.flux.operators.properties;

import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The operator properties. Support named-property, property with values.
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 05:32)
 */
public final class OperatorProperties {

    private Map<String, Property> properties = new LinkedHashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'")
            .withZone(ZoneId.of("UTC"));

    private OperatorProperties() {
    }

    @Nonnull
    public static OperatorProperties of() {
        return new OperatorProperties();
    }

    /**
     * @param fluxName      name in Flux query
     * @param namedProperty name in named properties
     * @see org.influxdb.flux.Flux#addPropertyNamed(String, String)
     */
    public void putPropertyNamed(@Nonnull final String fluxName, @Nonnull final String namedProperty) {

        Preconditions.checkNonEmptyString(fluxName, "Flux property name");
        Preconditions.checkNonEmptyString(namedProperty, "Named property");

        put(fluxName, new NamedProperty<>(namedProperty));
    }

    /**
     * @param fluxName name in Flux query
     * @param value    value of property. If null than ignored.
     * @see org.influxdb.flux.Flux#addPropertyValue(String, Object)
     */
    public void putPropertyValue(@Nonnull final String fluxName, @Nullable final Object value) {

        Preconditions.checkNonEmptyString(fluxName, "Flux property name");

        if (value == null) {
            return;
        }

        put(fluxName, (m) -> value);
    }

    /**
     * @param fluxName name of property in Flux query
     * @param amount   the amount of the duration, measured in terms of the unit, positive or negative
     * @param unit     the unit that the duration is measured in, must have an exact duration.  If null than ignored.
     * @see org.influxdb.flux.Flux#addPropertyValue(String, long, ChronoUnit)
     */
    public void putPropertyValue(@Nonnull final String fluxName,
                                 @Nullable final Long amount,
                                 @Nullable final ChronoUnit unit) {

        Preconditions.checkNonEmptyString(fluxName, "Flux property name");

        if (amount == null || unit == null) {
            return;
        }

        put(fluxName, (m) -> new TimeInterval(amount, unit));
    }

    /**
     * @param fluxName name of property in Flux query
     * @param value    value of property. If null than ignored.
     * @see org.influxdb.flux.Flux#addPropertyValueString(String, String)
     */
    public void putPropertyValueString(@Nonnull final String fluxName, @Nullable final String value) {

        Preconditions.checkNonEmptyString(fluxName, "Flux property name");

        if (value == null) {
            return;
        }

        put(fluxName, new StringProperty(value));
    }

    @Nonnull
    public Collection<String> keys() {
        return properties.keySet();
    }

    @Nullable
    public String get(@Nonnull final String key, @Nonnull final Map<String, Object> namedProperties) {

        Property property = properties.get(key);
        if (property == null) {
            return null;
        }

        Object value = property.value(namedProperties);
        if (value == null) {
            return null;
        }

        // array to collection
        if (value.getClass().isArray()) {
            value = Arrays.asList((Object[]) value);
        }

        // collection to delimited string ["one", "two", "three"]
        if (value instanceof Collection) {

            //noinspection unchecked
            Collection<Object> collection = (Collection<Object>) value;
            if (collection.isEmpty()) {
                return null;
            }

            value = collection.stream()
                    .map(host -> "\"" + host + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        if (value instanceof Instant) {
            value = DATE_FORMATTER.format((Instant) value);
        }

        return value.toString();
    }

    private void put(@Nonnull final String name, @Nullable final Property property) {

        if (property == null) {
            return;
        }

        properties.put(name, property);
    }

    private interface Property<T> {

        /**
         * @param namedProperties named property values
         * @return value of property
         */
        @Nullable
        T value(@Nonnull final Map<String, Object> namedProperties);
    }

    private final class NamedProperty<T> implements Property<T> {

        private final String parameterName;

        private NamedProperty(@Nonnull final String parameterName) {

            Preconditions.checkNonEmptyString(parameterName, "Parameter name");

            this.parameterName = parameterName;
        }

        @Nonnull
        @Override
        public T value(@Nonnull final Map<String, Object> namedProperties) {

            Object parameterValue = namedProperties.get(parameterName);
            // parameter must be defined
            if (parameterValue == null) {
                String message = String.format("The parameter '%s' is not defined.", parameterName);

                throw new IllegalStateException(message);
            }

            //noinspection unchecked
            return (T) parameterValue;
        }
    }

    private final class StringProperty implements Property<String> {

        private final String value;

        private StringProperty(@Nullable final String value) {
            this.value = value;
        }

        @Nullable
        @Override
        public String value(@Nonnull final Map<String, Object> namedProperties) {

            if (value == null) {
                return null;
            }

            return "\"" + value + "\"";
        }
    }
}