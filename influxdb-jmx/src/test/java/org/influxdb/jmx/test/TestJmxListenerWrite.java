package org.influxdb.jmx.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import org.influxdb.BatchOptions;
import org.influxdb.dto.Point;
import org.influxdb.jmx.JmxMonitorEventListener;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class TestJmxListenerWrite extends AbstractJmxListenerTest {

    private static final long WRITE_COUNT = 5000;

    @Test
    void testListenerInitialization() throws Exception {

        ObjectName objectName = new ObjectName(JmxMonitorEventListener.getMBeanName(MBEAN_NAME));
        ObjectInstance mBeanInfo = mBeanServer.getObjectInstance(objectName);

        Assert.assertNotNull(mBeanInfo);
        System.out.println(mBeanInfo);

        int count = 10;
        while (count > 0) {
            influxDB.write(getCpuStats());
            Thread.sleep(100);
            count--;
        }

        Object connectionCount = mBeanServer.getAttribute(objectName, "ConnectionCount");
        System.out.println("Connection count: " + connectionCount.toString());
        Assert.assertTrue(((int) connectionCount > 0));
        Assert.assertNotNull(connectionCount);
    }


    @Test
    void testWriteMultithreadedT20() throws Exception {
        influxDB.disableBatch();
        resetStatistics();
        testWriteMultithreaded(20, WRITE_COUNT);

        try {
            influxDB.write(Point.measurement("cpu")
                    .addField("ProcessCpuLoad", Double.NaN).build());
        } catch (Exception e) {
            System.out.println("Expected exception: " + e);
        }

        outStats();

        Assert.assertEquals("WriteCount", getClientStatisticsMBean().getWriteCount(), WRITE_COUNT);
        Assert.assertEquals("Host Address", options.getUrl(), getClientStatisticsMBean().getHostAddress());

        Assert.assertEquals("ErrorCount", getClientStatisticsMBean().getErrorCount(), 1);
        Assert.assertEquals("SuccessCount", getClientStatisticsMBean().getSuccessCount(), WRITE_COUNT);

    }

    @Test
    void testWriteBatchedMultithreadedT20() throws Exception {

        BatchOptions batchOptions = BatchOptions.DEFAULTS.actions(500).flushDuration(10_000);

        influxDB.enableBatch(batchOptions);
        resetStatistics();

        testWriteMultithreaded(20, WRITE_COUNT);
        influxDB.flush();
        outStats();


        Assert.assertEquals("No BusyConnections", getClientStatisticsMBean().getBusyConnectionCount(), 0);
        Assert.assertEquals("WriteCount", getClientStatisticsMBean().getWriteCount(),
                WRITE_COUNT / batchOptions.getActions());
        Assert.assertEquals("ErrorCount", getClientStatisticsMBean().getErrorCount(), 0);
        Assert.assertTrue("SuccessCount < " + WRITE_COUNT, getClientStatisticsMBean().getSuccessCount() < WRITE_COUNT);

    }

    @SuppressWarnings("SameParameterValue")
    private void testWriteMultithreaded(int threadCount, long writeCount) throws Exception {

        Runnable task = () -> {
            try {
                Point cpuStats = getCpuStats();
                influxDB.write(cpuStats);
            } catch (Exception e) {
                System.out.println(e.toString());
                throw new RuntimeException(e);
            }
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(threadCount);

        for (int i = 0; i < writeCount; i++) {
            scheduler.schedule(task, 0, TimeUnit.MILLISECONDS);
        }

        //wait to spawn some tasks
        Thread.sleep(100);

        //plan scheduler shutdown
        scheduler.shutdown();

        //wait until terminated
        while (!scheduler.isTerminated()) {
            if (isJmxEnabled()) {
                printConnectionPoolInfo();
            }
            Thread.sleep(100);
        }
        System.out.println("Finished all threads");

    }

    private boolean isJmxEnabled() throws Exception {
        ObjectName objectName = new
                ObjectName(JmxMonitorEventListener.getMBeanName(MBEAN_NAME));

        return mBeanServer.isRegistered(objectName);
    }

}
