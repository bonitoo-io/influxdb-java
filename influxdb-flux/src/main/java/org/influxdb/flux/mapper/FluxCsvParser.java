package org.influxdb.flux.mapper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class us used to construct FluxResult from CSV.
 */
class FluxCsvParser {

    @Nonnull
    FluxResult parseFluxResponse(@Nonnull final Reader reader) throws FluxResultMapperException, IOException {

        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();

        final LinkedHashMap<Integer, Table> tables = new LinkedHashMap<>();

        boolean startNewTable = false;
        Table table = null;

        int tableIndex = 0;
        for (int i = 0, recordsSize = records.size(); i < recordsSize; i++) {
            CSVRecord csvRecord = records.get(i);
            String token = csvRecord.get(0);
            //// start new table
            if ("#datatype".equals(token)) {
                startNewTable = true;

                table = new Table();
                tables.put(tableIndex, table);
                tableIndex++;

            } else if (table == null) {
                String message = "Unable to parse CSV response. Table definition was not found. Row:" + i;
                throw new FluxResultMapperException(message);
            }
            //#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string
            if ("#datatype".equals(token)) {
                table.setDataTypes(parseDataTypes(csvRecord));

            } else if ("#partition".equals(token)) {
                table.setPartitions(parsePartitions(csvRecord));

            } else if (token.startsWith("#")) {
                //TODO #default,_result,,,,,,,,, ???
                table.setDefaultEmptyValues(parseDefaultEmptyValues(csvRecord));

            } else {
                // parse column names
                if (startNewTable) {
                    parseColumnNamesAndTags(table, csvRecord);
                    startNewTable = false;
                    continue;
                }
                Record r = parseRecord(table, csvRecord);
                table.getRecords().add(r);
            }
        }
        return new FluxResult(tables);
    }

    private void parseColumnNamesAndTags(final Table table, final CSVRecord csvRecord) {

        int size = csvRecord.size();

        for (int i = 0; i < size; i++) {
            String columnName = csvRecord.get(i);

            table.addColumnName(columnName, i);

            if (!(columnName.startsWith("_")
                    || columnName.isEmpty()
                    || "result".equals(columnName)
                    || "table".equals(columnName))) {
                table.getTags().add(columnName);
            }
        }
    }

    private List<String> parseDefaultEmptyValues(final CSVRecord csvRecord) {
        //todo
        return null;
    }

    private Record parseRecord(final Table table, final CSVRecord csvRecord) {

        Record record = new Record();

        record.setField(getFieldVal(table, csvRecord, "_field"));
        record.setMeasurement(getFieldVal(table, csvRecord, "_measurement"));
        record.setStart(toInstant(getFieldVal(table, csvRecord, "_start")));
        record.setStop(toInstant(getFieldVal(table, csvRecord, "_stop")));
        record.setTime(toInstant(getFieldVal(table, csvRecord, "_time")));
        record.setValue(getFieldVal(table, csvRecord, "_value"));

        List<String> tags = table.getTags();

        for (String tag : tags) {
            String tagValue = csvRecord.get(table.indexOfColumn(tag));
            record.getTags().put(tag, tagValue);
        }
        return record;
    }

    private String getFieldVal(final Table table, final CSVRecord csvRecord, final String columnName) {

        int i = table.indexOfColumn(columnName);
        if (i > 0) {
            return csvRecord.get(i);
        }
        return null;
    }

    //parse RFC 3339 to instant
    @Nullable
    private Instant toInstant(@Nullable final String dateTime) {

        if (dateTime == null) {
            return null;
        }
        return Instant.parse(dateTime);
    }

    private List<String> parsePartitions(final CSVRecord csvRecord) {
        return toList(csvRecord);
    }

    private List<String> parseDataTypes(final CSVRecord csvRecord) {
        return toList(csvRecord);
    }

    private List<String> toList(final CSVRecord csvRecord) {
        List<String> ret = new ArrayList<>(csvRecord.size());
        int size = csvRecord.size();

        for (int i = 0; i < size; i++) {
            String rec = csvRecord.get(i);
            ret.add(rec);
        }
        return ret;
    }

}
