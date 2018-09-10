package org.influxdb.dto;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

import org.influxdb.ClosableChannel;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.TestUtils;
import org.influxdb.InfluxDB.LogLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Test for the Series.
 *
 * @author hoan.le [at] bonitoo.io
 *
 */
@RunWith(JUnitPlatform.class)
public class SeriesTest {
  private static final int BATCH_COUNT = 600;
  private InfluxDB influxDB;

  @BeforeEach
  public void setUp() {
    this.influxDB = InfluxDBFactory.connect("http://" + TestUtils.getInfluxIP() + ":" + TestUtils.getInfluxPORT(true),
        "root", "root");
    this.influxDB.setLogLevel(LogLevel.NONE);
  }

  @Test
  public void testWritePerformance() {
    String rp = TestUtils.defaultRetentionPolicy(this.influxDB.version());
    String dbName = "j_stress";
    influxDB.createDatabase(dbName);
    ClosableChannel queue = new ClosableChannel();

    long start = System.currentTimeMillis();
    new Thread(() -> {
      try {
        TreeMap<String, String> tags = new TreeMap<>();
        tags.put("tag0", "value0");
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");
        tags.put("tag3", "value3");
        long count = 0;
        for (int k = 0; k < BATCH_COUNT; k++) {
          BatchPoints batchPoints = BatchPoints.database(dbName).retentionPolicy(rp).build();
          for (int j = 0; j < 5000; j++) {
            Point point = Point.measurement("cpu").tag(tags).time(count++, TimeUnit.MILLISECONDS)
                .addField("idle", (double) j).addField("user", 2.0 * j).addField("system", 3.0 * j).build();
            batchPoints.point(point);
          }
          queue.put(batchPoints);
        }

        // Close queue.
        queue.close();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
    
    while (true) {
      try {
        Object o = queue.take();
        if (ClosableChannel.isClosingSinal(o)) {
          break;
        } else {
          influxDB.write((BatchPoints) o);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    
    System.out.println("testWritePerformance() takes " + (System.currentTimeMillis() - start));
  }
  
  @Test
  public void testSeriesPointWritePerformance() {
    String rp = TestUtils.defaultRetentionPolicy(this.influxDB.version());
    String dbName = "j_stress";
    influxDB.createDatabase(dbName);
    ClosableChannel queue = new ClosableChannel();
    
    long start = System.currentTimeMillis();
    
    new Thread(() -> {
      try {
        TreeMap<String, String> tags = new TreeMap<>();
        tags.put("tag0", "value0");
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");
        tags.put("tag3", "value3");
        Series series = new Series("cpu", tags);
        long count = 0;
        for (int k = 0; k < BATCH_COUNT; k++) {
          BatchPoints batchPoints = BatchPoints.database(dbName).retentionPolicy(rp).build();
          for (int j = 0; j < 5000; j++) {
            Point point = series.createPointBuilder().time(count++, TimeUnit.MILLISECONDS)
                .addField("idle", (double) j)
                .addField("user", 2.0 * j)
                .addField("system", 3.0 * j)
                .build();
            batchPoints.point(point);
          }
          queue.put(batchPoints);
        }

        // Close queue.
        queue.close();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
    
    while (true) {
      try {
        Object o = queue.take();
        if (ClosableChannel.isClosingSinal(o)) {
          break;
        } else {
          influxDB.write((BatchPoints) o);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      
    }
    
    System.out.println("testSeriesPointWritePerformance() takes " + (System.currentTimeMillis() - start));
  }
}
