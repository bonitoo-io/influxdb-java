package org.influxdb.jmx.test;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.jmx.JmxMonitorEventListener;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveFactory;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class ReactiveJmxTest extends AbstractJmxListenerTest {

    private InfluxDBReactive influxDBReactive;
    private BatchOptionsReactive batchOptionsReactive;

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

        batchOptionsReactive = BatchOptionsReactive.DEFAULTS;
        influxDBReactive = InfluxDBReactiveFactory.connect(options, batchOptionsReactive);
        influxDBReactive.query(new Query("CREATE DATABASE \"" + DB_NAME + "\"", null)).blockingSubscribe();

    }

    @AfterEach
    void onAfter() {
        resetStatistics();
        influxDBReactive.query(new Query("DROP DATABASE \"" + DB_NAME + "\"", null)).blockingSubscribe();
        influxDBReactive.close();
    }

    @Test
    void testJmxMbeanPresence() throws Exception {
        ObjectName objectName = new ObjectName(JmxMonitorEventListener.getMBeanName(MBEAN_NAME));
        ObjectInstance mBeanInfo = mBeanServer.getObjectInstance(objectName);
        Assert.assertNotNull(mBeanInfo);
        System.out.println(mBeanInfo);
        int count = 10;
        while (count > 0) {
            influxDBReactive.writePoint(getCpuStats());
            Thread.sleep(100);
            count--;
        }
        Object connectionCount = mBeanServer.getAttribute(objectName, "ConnectionCount");
        System.out.println("Connection count: " + connectionCount.toString());
        Assert.assertTrue(((int) connectionCount > 0));
        Assert.assertNotNull(connectionCount);
    }

    @Test
    void testJmxWriteCount() {
        resetStatistics();
        int count = 5000;
        Flowable<Point> objectFlowable =

                Flowable.range(1, 5000).map(i -> {
                    if (i % 100 == 0) {
                        System.out.println(i);
                    }
                    return i;
                }).flatMap(f -> Flowable.fromCallable(this::getCpuStats));

        influxDBReactive.writePoints(objectFlowable).subscribe();
        Assert.assertEquals("WriteCount", count / batchOptionsReactive.getActions(), getClientStatisticsMBean().getWriteCount());
    }

    @Test
    void testJmxQueryCount() throws Exception {

        int pointsCount = 100;
        prepareSomeData(pointsCount);
        Thread.sleep(1000);

        resetStatistics();
        Query query = new Query("SELECT * FROM cpu", DB_NAME);
        influxDBReactive.query(query).subscribe();
        influxDBReactive.query(query).subscribe();
        influxDBReactive.query(query).subscribe();
        Flowable<QueryResult> queryResultFlowable = influxDBReactive.query(query);


        Assert.assertEquals(pointsCount, queryResultFlowable.firstElement().blockingGet().getResults().get(0).getSeries().get(0).getValues().size());


        influxDBReactive.query(new Query("XSELECT * FROM cpu", DB_NAME))
                .doOnError(Throwable::printStackTrace)
                .subscribe();

        outStats();

        Assert.assertEquals("ErrorCount", getClientStatisticsMBean().getErrorCount(), 1);
        Assert.assertEquals("SuccessCount", getClientStatisticsMBean().getSuccessCount(), 4);
        Assert.assertEquals("QueryCount", getClientStatisticsMBean().getQueryCount(), 4);
    }

    private void prepareSomeData(int count) {
        for (int i = 0; i < count; i++) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {
            }
            influxDBReactive.writePoints(Flowable.fromCallable(this::getCpuStats)).blockingSubscribe();
        }
    }

    @Test
    void testJmxQueryCount2() throws Exception {

        int pointsCount = 1000;
        prepareSomeData(pointsCount);
        Thread.sleep(1000);

        resetStatistics();
        Query query = new Query("SELECT * FROM cpu", DB_NAME);

        int count = 20;

        Flowable.interval(1, TimeUnit.MILLISECONDS, Schedulers.newThread())
                .take(count)
                .map(i -> {
                    System.out.println(i +": " + Thread.currentThread());
                    return influxDBReactive.query(query).subscribe();
                })
                .blockingSubscribe();

        Flowable<QueryResult> queryResultFlowable = influxDBReactive.query(query);

        Assert.assertEquals(pointsCount, queryResultFlowable.firstElement().blockingGet().getResults().get(0).getSeries().get(0).getValues().size());


        influxDBReactive.query(new Query("XSELECT * FROM cpu", DB_NAME))
                .doOnError(Throwable::printStackTrace)
                .subscribe();

        outStats();

        Assert.assertEquals("ErrorCount", getClientStatisticsMBean().getErrorCount(), 1);
        Assert.assertEquals("SuccessCount", getClientStatisticsMBean().getSuccessCount(), count + 1);
        Assert.assertEquals("QueryCount", getClientStatisticsMBean().getQueryCount(), count + 1);
    }

}
