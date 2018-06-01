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
public interface InfluxDBServiceReactive extends InfluxDBService {

    /**
     * Write points into InfluxDB.
     *
     * @return the observable response
     * @see #writePoints(String, String, String, String, String, String, RequestBody)
     * @since 3.0.0
     */
    @POST("/write")
    @Nonnull
    Single<Response<String>> writePointsReactive(@Query(U) String username,
                                                 @Query(P) String password, @Query(DB) String database,
                                                 @Query(RP) String retentionPolicy, @Query(PRECISION) String precision,
                                                 @Query(CONSISTENCY) String consistency, @Body RequestBody batchPoints);
}
