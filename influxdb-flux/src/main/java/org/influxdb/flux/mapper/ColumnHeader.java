package org.influxdb.flux.mapper;

import java.time.Instant;

/**
 * This class represents column header specification of {@link Table}.
 */
public class ColumnHeader {


    //string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string

    private static final String STRING_DATATYPE = "string";
    private static final String DATETIME_DATATYPE = "dateTime:RFC3339";
    private static final String LONG_DATATYPE = "long";
    private static final String DOUBLE_DATATYPE = "double";

    //flux datatype
    private String dataType;

    //column index in csv
    private int index;

    //column name in csv
    private String columnName;

    //group
    private String group;
    private String defaultEmptyValue;
    private boolean tag;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    public Object toValue(final String strValue) throws FluxResultMapperException {

        if (STRING_DATATYPE.equals(dataType)) {
            return strValue;
        }

        if (DATETIME_DATATYPE.equals(dataType)) {
            return Instant.parse(strValue);
        }

        if (LONG_DATATYPE.equals(dataType)) {
            return Long.parseLong(strValue);
        }

        if (DOUBLE_DATATYPE.equals(dataType)) {
            return Double.parseDouble(strValue);
        }

        throw new FluxResultMapperException("Unsupported datatype: " + dataType);

    }

    public void addGroup(final String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setDefaultEmptyValue(final String defaultEmptyValue) {
        this.defaultEmptyValue = defaultEmptyValue;
    }

    public String getDefaultEmptyValue() {
        return defaultEmptyValue;
    }

    public void setTag(final boolean tag) {
        this.tag = tag;
    }

    public boolean getTag() {
        return tag;
    }
}
