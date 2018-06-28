package org.influxdb.flux.operators.restriction;

import org.influxdb.flux.operators.properties.OperatorProperties;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * The column restrictions.
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 13:04)
 */
public final class ColumnRestriction {

    private final String fieldName;

    ColumnRestriction(@Nonnull final String recordColumn) {

        Preconditions.checkNonEmptyString(recordColumn, "Record column");

        this.fieldName = recordColumn;
    }

    /**
     * Is column of record "equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions equal(@Nonnull final Object value) {
        return new OperatorRestrictions(fieldName, value, "==");
    }

    /**
     * Is column of record "not equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions notEqual(@Nonnull final Object value) {
        return new OperatorRestrictions(fieldName, value, "!=");
    }

    /**
     * Is column of record "less" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions less(@Nonnull final Object value) {
        return new OperatorRestrictions(fieldName, value, "<");
    }

    /**
     * Is column of record "greater" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions greater(@Nonnull final Object value) {
        return new OperatorRestrictions(fieldName, value, ">");
    }

    /**
     * Is column of record "less or equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions lessOrEqual(@Nonnull final Object value) {
        return new OperatorRestrictions(fieldName, value, "<=");
    }

    /**
     * Is column of record "greater or equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions greaterOrEqual(@Nonnull final Object value) {
        return new OperatorRestrictions(fieldName, value, ">=");
    }

    private final class OperatorRestrictions extends Restrictions {
        private final String fieldName;
        private final Object fieldValue;
        private final String operator;

        private OperatorRestrictions(@Nonnull final String fieldName,
                                     @Nonnull final Object fieldValue,
                                     @Nonnull final String operator) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.operator = operator;
        }

        @Override
        public String toString() {

            String value;
            if (fieldValue instanceof String) {
                value = "\"" + fieldValue + "\"";
            } else {
                value = OperatorProperties.serializeValue(fieldValue);
            }

            return "r[\"" + fieldName + "\"] " + operator + " " + value;
        }
    }
}
