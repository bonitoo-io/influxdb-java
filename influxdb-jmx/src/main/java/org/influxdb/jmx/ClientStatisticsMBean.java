package org.influxdb.jmx;


/**
 * <p>A standard MXBean interface for a InfluxDB client, for use on Java 6 and above virtual machines.</p>
 *
 * @since 3.0
 */
public interface ClientStatisticsMBean {

    /**
     * Gets the database host address.
     *
     * @return influxdb host address
     */
    String getHostAddress();

    /**
     * Resets all client statistics.
     */
    void reset();

    /**
     * Gets number of connections in the  connection pool (both active and inactive).
     *
     * @return number of connections
     */
    int getConnectionCount();

    /**
     * Gets number of inactive (idle) connections in the connection pool.
     *
     * @return number of idle connections
     */
    int getIdleConnectionCount();

    /**
     * Gets number of busy (active) connections in the  connection pool.
     *
     * @return number of busy connections
     */
    int getBusyConnectionCount();

    /**
     * Gets the number of write operations.
     */
    long getWriteCount();

    /**
     * Gets the number of batched writes operations.
     */
    long getBatchedCount();

    /**
     * Gets number of non batched write operations.
     */
    long getUnBatchedCount();

    /**
     * Gets the number of influxDB error responses.
     */
    long getErrorCount();

    /**
     * Gets the number of InfluxDb success responses.
     */
    long getSuccessCount();


}
