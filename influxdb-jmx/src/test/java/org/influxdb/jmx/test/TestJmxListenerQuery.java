package org.influxdb.jmx.test;

import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class TestJmxListenerQuery extends AbstractJmxListenerTest {

    private int pointsCount = 500;

    @Test
    void testCountErrorsSuccessQuery() throws Exception {
        influxDB.enableBatch();
        prepareSomeData();
        influxDB.flush();

        resetStatistics();
        Query query = new Query("SELECT * FROM cpu", DB_NAME);
        influxDB.query(query);
        influxDB.query(query);
        influxDB.query(query);
        QueryResult queryResult = influxDB.query(query);

        Assert.assertEquals(pointsCount, queryResult.getResults().get(0).getSeries().get(0).getValues().size());

        try {
            influxDB.query(new Query("XSELECT * FROM cpu", DB_NAME));
        } catch (Exception ignored) {
        }

        outStats();

        Assert.assertEquals("ErrorCount",getClientStatisticsMBean().getErrorCount(), 1);
        Assert.assertEquals("SuccessCount",getClientStatisticsMBean().getSuccessCount(), 4);
    }

    private void prepareSomeData() {
        for (int i = 0; i < pointsCount; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            influxDB.write(getCpuStats());
        }
    }

}
