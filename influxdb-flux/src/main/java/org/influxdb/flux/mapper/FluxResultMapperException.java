package org.influxdb.flux.mapper;

class FluxResultMapperException extends RuntimeException {
    FluxResultMapperException(final String s) {
        super(s);
    }

    FluxResultMapperException(final Exception e) {
        super(e);
    }
}
