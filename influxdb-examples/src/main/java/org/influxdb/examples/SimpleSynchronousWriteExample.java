package org.influxdb.examples;

import java.util.concurrent.TimeUnit;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

public class SimpleSynchronousWriteExample {

    public static void main(String[] args) {

        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086");
        influxDB.enableBatch(BatchOptions.DEFAULTS.actions(1000).bufferLimit(10000).flushDuration(1000));
        String dbName = "aTimeSeries";
        influxDB.createDatabase(dbName);

        influxDB.setDatabase(dbName);
        String rpName = "aRetentionPolicy";
        influxDB.createRetentionPolicy(rpName, dbName, "30d", "30m", 2, true);
        influxDB.setRetentionPolicy(rpName);

        influxDB.enableBatch(BatchOptions.DEFAULTS);

        influxDB.write(Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L)
                .build());

        influxDB.write(Point.measurement("disk")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("used", 80L)
                .addField("free", 1L)
                .build());

        influxDB.flush();

        Query query = new Query("SELECT * FROM cpu; SELECT * from disk", dbName);
        QueryResult queryResult = influxDB.query(query);

        System.out.println(queryResult);
        influxDB.dropRetentionPolicy(rpName, dbName);
        influxDB.deleteDatabase(dbName);
        influxDB.close();

    }

}
