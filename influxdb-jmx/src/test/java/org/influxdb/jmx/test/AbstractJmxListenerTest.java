package org.influxdb.jmx.test;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Point;
import org.influxdb.impl.InfluxDBImpl;
import org.influxdb.jmx.ClientStatisticsMBean;
import org.influxdb.jmx.JmxMonitorEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class AbstractJmxListenerTest {
    static final String DB_NAME = "jmx-test";
    static final String MBEAN_NAME = "test-mbean1";
    InfluxDB influxDB;
    InfluxDBOptions options;
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    @BeforeEach
    void setUp() {


        String hostname = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");

        options = InfluxDBOptions.builder()
                .url("http://" + hostname + ":" + influxdbPort)
                .username("admin")
                .password("admin")
                .database(DB_NAME)
                .addListener(new JmxMonitorEventListener(MBEAN_NAME))
                .build();

        influxDB = new InfluxDBImpl(options);
        //noinspection deprecation
        influxDB.createDatabase(DB_NAME);
    }

    @AfterEach
    void onAfter() {
        //noinspection deprecation
        influxDB.deleteDatabase(DB_NAME);
        influxDB.close();
    }


    Point getCpuStats() {

        Point.Builder p;
        try {
            ObjectName objectName = ObjectName.getInstance("java.lang:type=OperatingSystem");
            p = Point.measurement("cpu").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

            Number processCpuLoad = (Number) mBeanServer.getAttribute(objectName, "ProcessCpuLoad");
            if (!processCpuLoad.equals(Double.NaN)) {
                p.addField("ProcessCpuLoad", (double) processCpuLoad);
            }
            Number systemCpuLoad = (Number) mBeanServer.getAttribute(objectName, "SystemCpuLoad");
            if (!systemCpuLoad.equals(Double.NaN)) {
                p.addField("SystemCpuLoad", (double) systemCpuLoad);
            }
            Number systemLoadAverage = (Number) mBeanServer.getAttribute(objectName, "SystemLoadAverage");
            if (!systemLoadAverage.equals(Double.NaN)) {
                p.addField("SystemLoadAverage", (double) systemLoadAverage);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return p.build();

    }

    void resetStatistics() throws Exception {
        ClientStatisticsMBean bean = getClientStatisticsMBean();
        bean.reset();
    }

    ClientStatisticsMBean getClientStatisticsMBean() throws MalformedObjectNameException {
        ObjectName objectName = new
                ObjectName(JmxMonitorEventListener.getMBeanName(MBEAN_NAME));

        return JMX.newMBeanProxy(mBeanServer, objectName, ClientStatisticsMBean.class);
    }

    void outStats() throws Exception {
        System.out.println("Host Address: "+getClientStatisticsMBean().getHostAddress());
        System.out.println("Write count: " + getMBeanAttribute("WriteCount"));
        System.out.println("UnBatchedCount count: " + getMBeanAttribute("UnBatchedCount"));
        System.out.println("BatchedCount count: " + getMBeanAttribute("BatchedCount"));
        System.out.println("SuccessCount count: " + getMBeanAttribute("SuccessCount"));
        System.out.println("ErrorCount count: " + getMBeanAttribute("ErrorCount"));
    }

    Object getMBeanAttribute(String attr) throws Exception {

        ObjectName objectName = new
                ObjectName(JmxMonitorEventListener.getMBeanName(MBEAN_NAME));

        return mBeanServer.getAttribute(objectName, attr);
    }


}
