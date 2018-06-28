package org.influxdb.flux.mapper;

import javax.annotation.Nullable;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:52)
 */
public class FluxResult {

    private final String content;

    public FluxResult(@Nullable final String content) {
        this.content = content;
    }

    @Nullable
    public String getContent() {
        return content;
    }
}
