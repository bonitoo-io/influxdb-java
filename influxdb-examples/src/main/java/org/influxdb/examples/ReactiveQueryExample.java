package org.influxdb.examples;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Query;
import org.influxdb.reactive.InfluxDBReactive;

public class ReactiveQueryExample {


    public static void main(String[] args) {

        //create client
        InfluxDBReactive client = Utils.createInfluxDBReactive();

        //write bean into influxDB
        ServerMeasurement serverMeasurement = ServerMeasurement.getCurrentMeasurement();
        client.writeMeasurement(serverMeasurement);


        //write some testing measurement for 5 sec...
        Flowable<ServerMeasurement> serverInfos =
                Flowable.interval(250, TimeUnit.MILLISECONDS).map(i -> {
                    ServerMeasurement currentServerMeasurement = ServerMeasurement.getCurrentMeasurement();
                    System.out.println(currentServerMeasurement);
                    return currentServerMeasurement;
                });

        client.writeMeasurements(serverInfos);

        Utils.sleep(3000);

        Flowable<ServerMeasurement> result = client.query(new Query("SELECT * FROM host", Utils.EXAMPLE_DATABASE), ServerMeasurement.class);

        Single<List<ServerMeasurement>> listSingle = result.toList();
        List<ServerMeasurement> measurements = listSingle.blockingGet();

        for (ServerMeasurement measurement : measurements) {
            System.out.println(measurement);
        }

    }

}
