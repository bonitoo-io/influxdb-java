package org.influxdb.flux.mapper;

import java.util.List;

/**
 * This class represents the Flux result structure.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:52)
 */
public class FluxResult {

    private List<Table> tables;

    FluxResult(final List<Table> tables) {
        this.tables = tables;
    }

    public List<Table> getTables() {
        return tables;
    }
}
