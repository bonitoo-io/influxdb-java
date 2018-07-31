package org.influxdb.flux;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 10:03)
 */
@Measurement(name = "mem")
public class Memory {

    @Column(name = "time")
    public Instant time;

    @Column(name = "free")
    public Long free;

    @Column(name = "host", tag = true)
    public String host;

    @Column(name = "region", tag = true)
    public String region;
}