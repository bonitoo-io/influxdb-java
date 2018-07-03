package org.influxdb.flux.mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents table structure in FluxRequest.
 */
public class Table {

    //column header specification
    private List<ColumnHeader> columnHeaders = new ArrayList<>();

    //list of records
    private List<Record> records = new ArrayList<>();

    public List<ColumnHeader> getColumnHeaders() {
        return columnHeaders;
    }

    public List<Record> getRecords() {
        return records;
    }

    void addDataTypes(final List<String> datatypes) {

        for (int i = 0; i < datatypes.size(); i++) {
            String s = datatypes.get(i);

            ColumnHeader columnDef = new ColumnHeader();
            columnDef.setDataType(s);
            columnDef.setIndex(i);

            columnHeaders.add(columnDef);

        }
    }

    void addPartitions(final List<String> partitions) throws FluxResultMapperException {

        for (int i = 0; i < partitions.size(); i++) {
            String s = partitions.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #partition header";
                throw new FluxResultMapperException(message);
            }

            def.setPartition(s);
        }

    }

    void addDefaultEmptyValues(final List<String> defaultEmptyValues) throws FluxResultMapperException {

        for (int i = 0; i < defaultEmptyValues.size(); i++) {
            String s = defaultEmptyValues.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #partition header";
                throw new FluxResultMapperException(message);
            }

            def.setDefaultEmptyValue(s);
        }

    }

    void addColumnNamesAndTags(final List<String> columnNames) throws FluxResultMapperException {

        int size = columnNames.size();

        for (int i = 0; i < size; i++) {
            String columnName = columnNames.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #partition header";
                throw new FluxResultMapperException(message);
            }

            def.setColumnName(columnName);

            if (!(columnName.startsWith("_")
                    || columnName.isEmpty()
                    || "result".equals(columnName)
                    || "table".equals(columnName))) {
                def.setTag(true);
            }
        }
    }

}
