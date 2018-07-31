package org.influxdb.flux.mapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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

    /**
     * @return empty result
     */
    @Nonnull
    public static FluxResult empty() {
        return new FluxResult(new ArrayList<>());
    }

    public List<Table> getTables() {
        return tables;
    }
}
