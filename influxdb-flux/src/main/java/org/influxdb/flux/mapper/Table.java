package org.influxdb.flux.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents table structure in FluxRequest.
 */
public class Table {

    //index of table in result
    long index;

    //types
    private List<String> dataTypes = new ArrayList<>();

    //partitions
    private List<String> partitions = new ArrayList<>();

    //types
    private Map<String, Integer> columnNames = new LinkedHashMap<>();

    //list of empty values
    private List<String> defaultEmptyValues = new ArrayList<>();

    //list of records
    private List<Record> records = new ArrayList<>();

    //tags
    private List<String> tags = new ArrayList<>();

    Table() {

    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public List<String> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(List<String> dataTypes) {
        this.dataTypes = dataTypes;
    }

    public List<String> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<String> partitions) {
        this.partitions = partitions;
    }

    public List<Record> getRecords() {
        return records;
    }

    void setDefaultEmptyValues(List<String> emptyValues) {
        this.defaultEmptyValues = emptyValues;
    }

    void addColumnName(String columnName, int index) {
        columnNames.put(columnName, index);
    }

    public String[] getColumnNames() {
        return (String[]) columnNames.keySet().toArray();
    }

    public List<String> getDefaultEmptyValues() {
        return defaultEmptyValues;
    }

    public List<String> getTags() {
        return tags;
    }

    int indexOfColumn(String field) {
        Integer ret = columnNames.get(field);
        if (ret == null) {
            return -1;
        }
        return ret;
    }
}
