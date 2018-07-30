package org.influxdb.flux.mapper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.influxdb.flux.options.FluxCsvParserOptions;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class us used to construct FluxResult from CSV.
 */
class FluxCsvParser {

    @Nonnull
    FluxResult parseFluxResponse(@Nonnull final Reader reader) throws FluxResultMapperException, IOException {
        return parseFluxResponse(reader, FluxCsvParserOptions.DEFAULTS);
    }

    @Nonnull
    FluxResult parseFluxResponse(@Nonnull final Reader reader, @Nonnull final FluxCsvParserOptions settings)
            throws FluxResultMapperException, IOException {

        Objects.requireNonNull(reader, "Reader is required");
        Objects.requireNonNull(settings, "FluxCsvParserOptions is required");

        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();

        final List<Table> tables = new ArrayList<>();

        boolean startNewTable = false;
        Table table = null;

        int tableIndex = 0;
        int tableColumnIndex = -1;

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

            } else if ("#group".equals(token)) {
                table.addGroups(toList(csvRecord));

            } else if (token.startsWith("#")) {
                table.addDefaultEmptyValues(toList(csvRecord));

            } else {
                // parse column names
                if (startNewTable) {
                    tableColumnIndex = table.addColumnNamesAndTags(toList(csvRecord), settings);
                    startNewTable = false;
                    continue;
                }

                //create the new table object if tableColumnIndex is incremented
                if (tableColumnIndex < 0) {
                    throw new FluxResultMapperException("table index is not found in CSV header");
                }

                int currentIndex = Integer.parseInt(csvRecord.get(tableColumnIndex));

                if (currentIndex > (tableIndex - 1)) {
                    //create new table with previous column headers settings
                    List<ColumnHeader> columnHeaders = table.getColumnHeaders();
                    table = new Table();
                    table.setColumnHeaders(columnHeaders);
                    tables.add(tableIndex, table);
                    tableIndex++;
                }

                Record r = parseRecord(table, csvRecord, settings);
                table.getRecords().add(r);
            }
        }
        return new FluxResult(tables);
    }

    private Record parseRecord(final Table table, final CSVRecord csvRecord, final FluxCsvParserOptions settings)
            throws FluxResultMapperException {

        Record record = new Record();

        List<String> valueDestinations = settings.getValueDestinations();

        for (ColumnHeader columnHeader : table.getColumnHeaders()) {

            int index = columnHeader.getIndex();
            String columnName = columnHeader.getColumnName();

            if ("_field".equals(columnName)) {
                record.setField(csvRecord.get(index));
            } else if ("_measurement".equals(columnName)) {
                record.setMeasurement(csvRecord.get(index));
            } else if ("_start".equals(columnName)) {
                record.setStart((Instant) columnHeader.toValue(csvRecord.get(index)));
            } else if ("_stop".equals(columnName)) {
                record.setStop((Instant) columnHeader.toValue(csvRecord.get(index)));
            } else if ("_time".equals(columnName)) {
                record.setTime((Instant) columnHeader.toValue(csvRecord.get(index)));
            } else if (columnHeader.getTag()) {
                record.getTags().put(columnName, csvRecord.get(index));
            }

            // values
            //
            // Record can have multiple values see:
            // org.influxdb.flux.options.FluxCsvParserOptions.Builder.valueDestinations
            if (valueDestinations.contains(columnName)) {
                record.getValues().put(columnName, columnHeader.toValue(csvRecord.get(index)));

                if (valueDestinations.get(0).equals(columnName)) {
                    record.setValue(columnHeader.toValue(csvRecord.get(index)));
                }
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
