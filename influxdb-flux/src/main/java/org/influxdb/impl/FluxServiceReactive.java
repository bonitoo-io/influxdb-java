package org.influxdb.impl;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:33)
 * @since 3.0.0
 */
public interface FluxServiceReactive {

    @Streaming
    @POST("/v1/query")
    @Nonnull
    Observable<ResponseBody> query(@Query(value = InfluxDBService.Q, encoded = true) String query,
                                   @Query(value = "orgID", encoded = true) String orgID);
}
