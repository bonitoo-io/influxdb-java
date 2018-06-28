package org.influxdb.flux.operators.restriction;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 13:03)
 */
public abstract class Restrictions {

    Restrictions() {
    }

    @Nonnull
    public static Restrictions and(@Nonnull final Restrictions... restrictions) {
        return new Logical("AND", restrictions);
    }

    @Nonnull
    public static Restrictions or(@Nonnull final Restrictions... restrictions) {
        return new Logical("OR", restrictions);
    }

    /**
     * Create Record measurement restriction.
     *
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction measurement() {
        return new ColumnRestriction("_measurement");
    }

    /**
     * Create Record field restriction.
     *
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction field() {
        return new ColumnRestriction("_field");
    }

    /**
     * Create Record start restriction.
     *
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction start() {
        return new ColumnRestriction("_start");
    }

    /**
     * Create Record stop restriction.
     *
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction stop() {
        return new ColumnRestriction("_stop");
    }

    /**
     * Create Record time restriction.
     *
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction time() {
        return new ColumnRestriction("_time");
    }

    /**
     * Create Record value restriction.
     *
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction value() {
        return new ColumnRestriction("_value");
    }

    /**
     * Create Record tag restriction.
     *
     * @param tagName tag name
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction tag(@Nonnull final String tagName) {
        return column(tagName);
    }

    /**
     * Create Record column restriction.
     *
     * @param columnName column name
     * @return restriction
     */
    @Nonnull
    public static ColumnRestriction column(@Nonnull final String columnName) {
        return new ColumnRestriction(columnName);
    }

    private static class Logical extends Restrictions {

        private final String operator;
        private final Restrictions[] restrictions;

        Logical(@Nonnull final String operator, @Nonnull final Restrictions... restrictions) {
            super();
            this.operator = operator;
            this.restrictions = restrictions;
        }

        @Override
        public String toString() {

            return Stream.of(restrictions)
                    .map(Object::toString)
                    .collect(Collectors.joining(" " + operator + " ", "(", ")"));
        }
    }
}
