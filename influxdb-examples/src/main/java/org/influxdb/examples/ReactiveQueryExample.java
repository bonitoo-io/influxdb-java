package org.influxdb.examples;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveFactory;

public class ReactiveQueryExample {

    public static void main(String[] args) throws Exception {

        String databaseName = "influxdb_example";
        //create client
        InfluxDBOptions options = InfluxDBOptions.builder()
                .username("admin")
                .password("admin")
                .database(databaseName)
                .url("http://localhost:8086").build();

        InfluxDBReactive client = InfluxDBReactiveFactory.connect(options);

        //cleanup / drop measurement
        client.query(new Query("DROP MEASUREMENT host", databaseName)).subscribe();

        //write bean into influxDB
        ServerMeasurement serverMeasurement = ServerMeasurement.getCurrentMeasurement();
        client.writeMeasurement(serverMeasurement);

        //write 10 measurements with 250ms delay...
        Flowable<ServerMeasurement> serverInfos =
                Flowable.range(0, 10)
                        .delay(250, TimeUnit.MILLISECONDS, Schedulers.trampoline())
                        .map(i -> {
                            ServerMeasurement currentServerMeasurement = ServerMeasurement.getCurrentMeasurement();
                            System.out.println(currentServerMeasurement);
                            return currentServerMeasurement;
                        });

        // write measurement flowable
        client.writeMeasurements(serverInfos);

        Thread.sleep(3000);
        System.out.println("--------- result ---------");

        Flowable<ServerMeasurement> result = client.query(new Query("SELECT * FROM host", databaseName),
                ServerMeasurement.class);

        result.subscribe(System.out::println);

    }

}
