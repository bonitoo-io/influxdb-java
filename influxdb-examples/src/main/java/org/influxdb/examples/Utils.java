package org.influxdb.examples;


import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBReactiveImpl;
import org.influxdb.reactive.InfluxDBReactiveFactory;
import org.influxdb.reactive.InfluxDBReactiveListenerDefault;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveListener;

public class Utils {

    public static String EXAMPLE_DATABASE = "influxdb_example";

    public static OperatingSystemMXBean getOperatingSystemMBean() {
        try {
            ObjectName objectName = ObjectName.getInstance("java.lang:type=OperatingSystem");

            return JMX.newMBeanProxy(ManagementFactory.getPlatformMBeanServer(), objectName, OperatingSystemMXBean.class);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }

    }

    public static void createDatabase(InfluxDBOptions options, String databaseName) {
        InfluxDBFactory.connect(options).query(new Query("CREATE DATABASE " + databaseName, null));
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception ignored) {

        }
    }

    public static InfluxDBReactive createInfluxDBReactive() {
        return createInfluxDBReactive(BatchOptionsReactive.DEFAULTS, new InfluxDBReactiveListenerDefault());
    }

    public static InfluxDBReactive createInfluxDBReactive(BatchOptionsReactive batchOptionsReactive, InfluxDBReactiveListener listener) {

        InfluxDBOptions options = InfluxDBOptions.builder().
                url("http://localhost:8086").
                database(EXAMPLE_DATABASE).
                username("admin").
                password("admin").build();

        InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory
                .connect(options, batchOptionsReactive, listener);

        Utils.createDatabase(options, EXAMPLE_DATABASE);

        return influxDBReactive;

    }

}
