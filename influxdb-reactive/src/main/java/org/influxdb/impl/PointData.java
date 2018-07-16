package org.influxdb.impl;

import org.influxdb.dto.Point;
import org.influxdb.reactive.options.WriteOptions;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (18/06/2018 14:57)
 */
final class PointData extends AbstractData<Point> {

    private Point point;

    PointData(@Nonnull final Point point, @Nonnull final WriteOptions writeOptions) {
        super(writeOptions);
        this.point = point;
    }

    @Nonnull
    @Override
    Point getData() {
        return point;
    }

    @Nonnull
    @Override
    String lineProtocol() {
        return point.lineProtocol(writeOptions.getPrecision());
    }
}
