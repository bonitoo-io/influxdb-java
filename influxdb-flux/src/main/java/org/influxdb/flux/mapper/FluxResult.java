package org.influxdb.flux.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:52)
 */
public class FluxResult {

    private LinkedHashMap<Integer, Table> tables;

    FluxResult(final LinkedHashMap<Integer, Table> tables) {
        this.tables = tables;
    }

    public Table getTable(final int index) {
        return tables.get(index);
    }

    public List<Table> getTables() {
        return new ArrayList<>(tables.values());
    }
}
