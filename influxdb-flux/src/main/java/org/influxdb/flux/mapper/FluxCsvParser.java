package org.influxdb.flux.mapper;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * This class us used to construct FluxResult from CSV.
 */
class FluxCsvParser {

    @Nonnull
    FluxResult parseFluxResponse(@Nonnull final Reader reader) throws FluxResultMapperException, IOException {

        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();

        final List<Table> tables = new ArrayList<>();

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
                tables.add(tableIndex, table);
                tableIndex++;

            } else if (table == null) {
                String message = "Unable to parse CSV response. Table definition was not found. Row:" + i;
                throw new FluxResultMapperException(message);
            }
            //#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string
            if ("#datatype".equals(token)) {
                table.addDataTypes(toList(csvRecord));

            } else if ("#partition".equals(token)) {
                table.addPartitions(toList(csvRecord));

            } else if (token.startsWith("#")) {
                table.addDefaultEmptyValues(toList(csvRecord));

            } else {
                // parse column names
                if (startNewTable) {
                    table.addColumnNamesAndTags(toList(csvRecord));
                    startNewTable = false;
                    continue;
                }
                Record r = parseRecord(table, csvRecord);
                table.getRecords().add(r);
            }
        }
        return new FluxResult(tables);
    }


    private List<String> parseDefaultEmptyValues(final CSVRecord csvRecord) {
        //todo
        return null;
    }

    private Record parseRecord(final Table table, final CSVRecord csvRecord) throws FluxResultMapperException {

        Record record = new Record();

        for (ColumnHeader columnHeader : table.getColumnHeaders()) {

            int index = columnHeader.getIndex();
            String columnName = columnHeader.getColumnName();

            if ("_field".equals(columnName)) {
                record.setField(csvRecord.get(index));
            } else if ("_measurement".equals(columnName)) {
                record.setMeasurement(csvRecord.get(index));
            } else if ("_value".equals(columnName)) {
                record.setValue(columnHeader.toValue(csvRecord.get(index)));
            } else if ("_start".equals(columnName)) {
                record.setStart((Instant) columnHeader.toValue(csvRecord.get(index)));
            } else if ("_stop".equals(columnName)) {
                record.setStop((Instant) columnHeader.toValue(csvRecord.get(index)));
            } else if ("_time".equals(columnName)) {
                record.setTime((Instant) columnHeader.toValue(csvRecord.get(index)));
            } else if (columnHeader.getTag()) {
                record.getTags().put(columnName, csvRecord.get(index));
            }
        }
        return record;
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
