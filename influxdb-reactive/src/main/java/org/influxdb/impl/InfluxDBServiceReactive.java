package org.influxdb.impl;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 11:56)
 * @since 3.0.0
 */
public interface InfluxDBServiceReactive {

    @POST("/write")
    @Nonnull
    Completable writePoints(@Query(InfluxDBService.U) String username,
                            @Query(InfluxDBService.P) String password,
                            @Query(InfluxDBService.DB) String database,
                            @Query(InfluxDBService.RP) String retentionPolicy,
                            @Query(InfluxDBService.PRECISION) String precision,
                            @Query(InfluxDBService.CONSISTENCY) String consistency,
                            @Body RequestBody points);

    @Streaming
    @GET("/query?chunked=true")
    @Nonnull
    Observable<ResponseBody> query(@Query(InfluxDBService.U) String username,
                                   @Query(InfluxDBService.P) String password,
                                   @Query(InfluxDBService.DB) String db,
                                   @Query(InfluxDBService.EPOCH) String epoch,
                                   @Query(InfluxDBService.CHUNK_SIZE) int chunkSize,
                                   @Query(value = InfluxDBService.Q, encoded = true) String query,
                                   @Query(value = InfluxDBService.PARAMS, encoded = true) String params);

    @GET("/ping")
    Maybe<Response<ResponseBody>> ping();
}
