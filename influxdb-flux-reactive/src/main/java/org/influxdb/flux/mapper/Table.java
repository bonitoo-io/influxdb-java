package org.influxdb.flux.mapper;

import org.influxdb.flux.options.FluxCsvParserOptions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    void setColumnHeaders(final List<ColumnHeader> columnHeaders) {
        this.columnHeaders = columnHeaders;
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

    void addGroups(final List<String> groups) throws FluxResultMapperException {

        for (int i = 0; i < groups.size(); i++) {
            String s = groups.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #group header";
                throw new FluxResultMapperException(message);
            }

            def.addGroup(s);
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
                String message = "Unable to parse response, inconsistent  #datatypes and #group header";
                throw new FluxResultMapperException(message);
            }

            def.setDefaultEmptyValue(s);
        }

    }

    /**
     * Sets the column names and tags and returns index of "table" column.
     *
     * @param columnNames
     * @param settings    of parsing
     * @return index of "table" column
     * @throws FluxResultMapperException
     */
    int addColumnNamesAndTags(final List<String> columnNames, @Nonnull final FluxCsvParserOptions settings)
            throws FluxResultMapperException {

        Objects.requireNonNull(settings, "FluxCsvParserOptions is required");

        int size = columnNames.size();
        int tableIndexColumn = -1;

        for (int i = 0; i < size; i++) {
            String columnName = columnNames.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #group header";
                throw new FluxResultMapperException(message);
            }

            def.setColumnName(columnName);

            if ("table".equals(columnName)) {
                tableIndexColumn = i;
            }

            if (!(columnName.startsWith("_")
                    || columnName.isEmpty()
                    || "result".equals(columnName)
                    || "table".equals(columnName)
                    || settings.getValueDestinations().contains(columnName))) {
                def.setTag(true);
            }
        }

        return tableIndexColumn;
    }

}
