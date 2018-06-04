package org.influxdb.impl;

import io.reactivex.Single;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 11:56)
 */
public interface InfluxDBServiceReactive {

    /**
     * Write points into InfluxDB.
     *
     * @return the observable response
     * @see InfluxDBService#writePoints(String, String, String, String, String, String, RequestBody)
     * @since 3.0.0
     */
    @POST("/write")
    @Nonnull
    Single<Response<String>> writePoints(@Query(InfluxDBService.U) String username,
                                         @Query(InfluxDBService.P) String password,
                                         @Query(InfluxDBService.DB) String database,
                                         @Query(InfluxDBService.RP) String retentionPolicy,
                                         @Query(InfluxDBService.PRECISION) String precision,
                                         @Query(InfluxDBService.CONSISTENCY) String consistency,
                                         @Body RequestBody points);
}
