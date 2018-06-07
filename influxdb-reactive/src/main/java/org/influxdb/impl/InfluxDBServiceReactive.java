package org.influxdb.impl;

import io.reactivex.Completable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 11:56)
 * @since 3.0.0
 */
public interface InfluxDBServiceReactive {

    /**
     * Write points into InfluxDB.
     *
     * @param username        u: optional The username for authentication
     * @param password        p: optional The password for authentication
     * @param database        db: required The database to write points
     * @param retentionPolicy rp: optional The retention policy to write points.
     *                        If not specified, the autogen retention
     * @param precision       optional The precision of the time stamps (n, u, ms, s, m, h).
     *                        If not specified, n
     * @param consistency     optional The write consistency level required for the write to succeed.
     *                        Can be one of one, any, all, quorum. Defaults to all.
     * @param points          The points that will be write into InfluxDB
     * @return the observable response
     * @see InfluxDBService#writePoints(String, String, String, String, String, String, RequestBody)
     * @since 3.0.0
     */
    @POST("/write")
    @Nonnull
    Completable writePoints(@Query(InfluxDBService.U) String username,
                            @Query(InfluxDBService.P) String password,
                            @Query(InfluxDBService.DB) String database,
                            @Query(InfluxDBService.RP) String retentionPolicy,
                            @Query(InfluxDBService.PRECISION) String precision,
                            @Query(InfluxDBService.CONSISTENCY) String consistency,
                            @Body RequestBody points);
}
