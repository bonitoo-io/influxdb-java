influxdb-java
=============

[![Build Status](https://travis-ci.org/influxdata/influxdb-java.svg?branch=master)](https://travis-ci.org/influxdata/influxdb-java)
[![codecov.io](http://codecov.io/github/influxdata/influxdb-java/coverage.svg?branch=master)](http://codecov.io/github/influxdata/influxdb-java?branch=master)
[![Issue Count](https://codeclimate.com/github/influxdata/influxdb-java/badges/issue_count.svg)](https://codeclimate.com/github/influxdata/influxdb-java)

This is the Java Client library which is only compatible with InfluxDB 0.9 and higher. Maintained by [@majst01](https://github.com/majst01).

To connect to InfluxDB 0.8.x you need to use influxdb-java version 1.6.

This implementation is meant as a Java rewrite of the influxdb-go package.
All low level REST Api calls are available.

## Usage

### Basic Usage:
 
This is a recommended approach to write data points into InfluxDB. The influxdb-java 
client is storing your writes into an internal buffer and flushes them asynchronously 
to InfluxDB at a fixed flush interval to achieve good performance on both client and 
server side. This requires influxdb-java v2.7 or newer.

If you want to write data points immediately into InfluxDB and synchronously process
resulting errors see [this section.](#synchronous-writes)

```java
InfluxDB influxDB = InfluxDBFactory.connect("http://172.17.0.2:8086", "root", "root");
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

Query query = new Query("SELECT idle FROM cpu", dbName);
influxDB.query(query);
influxDB.dropRetentionPolicy(rpName, dbName);
influxDB.deleteDatabase(dbName);
influxDB.close();
```


Any errors that happen during the batch flush won't leak into the caller of the `write` method. By default, any kind of errors will be just logged with "SEVERE" level.
If you need to be notified and do some custom logic when such asynchronous errors happen, you can add an error handler with a `BiConsumer<Iterable<Point>, Throwable>` using the overloaded `enableBatch` method:

```java
influxDB.enableBatch(BatchOptions.DEFAULTS.exceptionHandler(
        (failedPoints, throwable) -> { /* custom error handling here */ })
);
```

With batching enabled the client provides two strategies how to deal with errors thrown by the InfluxDB server. 

   1. 'One shot' write - on failed write request to InfluxDB server an error is reported to the client using the means mentioned above.        
   2. 'Retry on error' write (used by default) - on failed write the request by the client is repeated after batchInterval elapses 
       (if there is a chance the write will succeed - the error was caused by overloading the server, a network error etc.) 
       When new data points are written before the previous (failed) points are successfully written, those are queued inside the client 
       and wait until older data points are successfully written. 
       Size of this queue is limited and configured by `BatchOptions.bufferLimit` property. When the limit is reached, the oldest points
       in the queue are dropped. 'Retry on error' strategy is used when individual write batch size defined by `BatchOptions.actions` is lower than `BatchOptions.bufferLimit`.

Note:
* Batching functionality creates an internal thread pool that needs to be shutdown explicitly as part of a graceful application shut-down, or the application will not shut down properly. To do so simply call: ```influxDB.close()```
* `InfluxDB.enableBatch(BatchOptions)` is available since version 2.9. Prior versions use `InfluxDB.enableBatch(actions, flushInterval, timeUnit)` or similar based on the configuration parameters you want to set. 
* APIs to create and drop retention policies are supported only in versions > 2.7
* If you are using influxdb < 2.8, you should use retention policy: 'autogen'
* If you are using influxdb < 1.0.0, you should use 'default' instead of 'autogen'

If your points are written into different databases and retention policies, the more complex InfluxDB.write() methods can be used:

```java
InfluxDB influxDB = InfluxDBFactory.connect("http://172.17.0.2:8086", "root", "root");
String dbName = "aTimeSeries";
influxDB.createDatabase(dbName);
String rpName = "aRetentionPolicy";
influxDB.createRetentionPolicy(rpName, dbName, "30d", "30m", 2, true);

// Flush every 2000 Points, at least every 100ms
influxDB.enableBatch(BatchOptions.DEFAULTS.actions(2000).flushDuration(100));

Point point1 = Point.measurement("cpu")
					.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.addField("idle", 90L)
					.addField("user", 9L)
					.addField("system", 1L)
					.build();
Point point2 = Point.measurement("disk")
					.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.addField("used", 80L)
					.addField("free", 1L)
					.build();

influxDB.write(dbName, rpName, point1);
influxDB.write(dbName, rpName, point2);
Query query = new Query("SELECT idle FROM cpu", dbName);
influxDB.query(query);
influxDB.dropRetentionPolicy(rpName, dbName);
influxDB.deleteDatabase(dbName);
influxDB.close();
```
 

#### Synchronous writes

If you want to write the data points immediately to InfluxDB (and handle the errors as well) without any delays see the following example: 

```java
InfluxDB influxDB = InfluxDBFactory.connect("http://172.17.0.2:8086", "root", "root");
String dbName = "aTimeSeries";
influxDB.createDatabase(dbName);
String rpName = "aRetentionPolicy";
influxDB.createRetentionPolicy(rpName, dbName, "30d", "30m", 2, true);

BatchPoints batchPoints = BatchPoints
				.database(dbName)
				.tag("async", "true")
				.retentionPolicy(rpName)
				.consistency(ConsistencyLevel.ALL)
				.build();
Point point1 = Point.measurement("cpu")
					.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.addField("idle", 90L)
					.addField("user", 9L)
					.addField("system", 1L)
					.build();
Point point2 = Point.measurement("disk")
					.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.addField("used", 80L)
					.addField("free", 1L)
					.build();
batchPoints.point(point1);
batchPoints.point(point2);
influxDB.write(batchPoints);
Query query = new Query("SELECT idle FROM cpu", dbName);
influxDB.query(query);
influxDB.dropRetentionPolicy(rpName, dbName);
influxDB.deleteDatabase(dbName);
```

### Advanced Usage:

#### Gzip's support (version 2.5+ required):

influxdb-java client doesn't enable gzip compress for http request body by default. If you want to enable gzip to reduce transfer data's size , you can call:
```java
influxDB.enableGzip()
```

#### UDP's support (version 2.5+ required):

influxdb-java client support udp protocol now. you can call following methods directly to write through UDP.
```java
public void write(final int udpPort, final String records);
public void write(final int udpPort, final List<String> records);
public void write(final int udpPort, final Point point);
```
note: make sure write content's total size should not > UDP protocol's limit(64K), or you should use http instead of udp.


#### Chunking support (version 2.6+ required):

influxdb-java client now supports influxdb chunking. The following example uses a chunkSize of 20 and invokes the specified Consumer (e.g. System.out.println) for each received QueryResult
```java
Query query = new Query("SELECT idle FROM cpu", dbName);
influxDB.query(query, 20, queryResult -> System.out.println(queryResult));
```

#### QueryResult mapper to POJO (version 2.7+ required):

An alternative way to handle the QueryResult object is now available.
Supposing that you have a measurement _CPU_:
```
> INSERT cpu,host=serverA,region=us_west idle=0.64,happydevop=false,uptimesecs=123456789i
>
> select * from cpu
name: cpu
time                           happydevop host    idle region  uptimesecs
----                           ---------- ----    ---- ------  ----------
2017-06-20T15:32:46.202829088Z false      serverA 0.64 us_west 123456789
```
And the following tag keys:
```
> show tag keys from cpu
name: cpu
tagKey
------
host
region
```

1. Create a POJO to represent your measurement. For example:
```Java
public class Cpu {
    private Instant time;
    private String hostname;
    private String region;
    private Double idle;
    private Boolean happydevop;
    private Long uptimeSecs;
    // getters (and setters if you need)
}
```
2. Add @Measurement and @Column annotations:
```Java
@Measurement(name = "cpu")
public class Cpu {
    @Column(name = "time")
    private Instant time;
    @Column(name = "host", tag = true)
    private String hostname;
    @Column(name = "region", tag = true)
    private String region;
    @Column(name = "idle")
    private Double idle;
    @Column(name = "happydevop")
    private Boolean happydevop;
    @Column(name = "uptimesecs")
    private Long uptimeSecs;
    // getters (and setters if you need)
}
```
3. Call _InfluxDBResultMapper.toPOJO(...)_ to map the QueryResult to your POJO:
```
InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
String dbName = "myTimeseries";
QueryResult queryResult = influxDB.query(new Query("SELECT * FROM cpu", dbName));

InfluxDBResultMapper resultMapper = new InfluxDBResultMapper(); // thread-safe - can be reused
List<Cpu> cpuList = resultMapper.toPOJO(queryResult, Cpu.class);
```
**QueryResult mapper limitations**
- If your InfluxDB query contains multiple SELECT clauses, you will have to call InfluxResultMapper#toPOJO() multiple times to map every measurement returned by QueryResult to the respective POJO;
- If your InfluxDB query contains multiple SELECT clauses **for the same measurement**, InfluxResultMapper will process all results because there is no way to distinguish which one should be mapped to your POJO. It may result in an invalid collection being returned;
- A Class field annotated with _@Column(..., tag = true)_ (i.e. a [InfluxDB Tag](https://docs.influxdata.com/influxdb/v1.2/concepts/glossary/#tag-value)) must be declared as _String_.
-- _Note: With the current released version (2.7), InfluxDBResultMapper does not support QueryResult created by queries using the "GROUP BY" clause. This was fixed by [PR #345](https://github.com/influxdata/influxdb-java/pull/345)._

#### Query using Callbacks (version 2.8+ required)

influxdb-java now supports returning results of a query via callbacks. Only one
of the following consumers are going to be called once :

```java
this.influxDB.query(new Query("SELECT idle FROM cpu", dbName), queryResult -> {
    // Do something with the result...
}, throwable -> {
    // Do something with the error...
});
```

#### Query using parameter binding ("prepared statements", version 2.10+ required)

If your Query is based on user input, it is good practice to use parameter binding to avoid [injection attacks](https://en.wikipedia.org/wiki/SQL_injection).
You can create queries with parameter binding with the help of the QueryBuilder:

```java
Query query = QueryBuilder.newQuery("SELECT * FROM cpu WHERE idle > $idle AND system > $system") 
        .forDatabase(dbName)
        .bind("idle", 90)
        .bind("system", 5)
        .create();
QueryResult results = influxDB.query(query);
```

The values of the bind() calls are bound to the placeholders in the query ($idle, $system). 

#### Batch flush interval jittering (version 2.9+ required)

When using large number of influxdb-java clients against a single server it may happen that all the clients 
will submit their buffered points at the same time and possibly overloading the server. This is usually happening
when all the clients are started at once - for instance as members of cloud hosted large cluster networks.  
If all the clients have the same flushDuration set this situation will repeat periodically.

To solve this situation the influxdb-java offers an option to offset the flushDuration by a random interval so that 
the clients will flush their buffers in different intervals:    

```java
influxDB.enableBatch(BatchOptions.DEFAULTS.jitterDuration(500);
```

### Other Usages:
For additional usage examples have a look at [InfluxDBTest.java](https://github.com/influxdb/influxdb-java/blob/master/src/test/java/org/influxdb/InfluxDBTest.java "InfluxDBTest.java") or [influxdb-examples](https://github.com/bonitoo-io/influxdb-java/tree/prototype-reactive/influxdb-examples/src/main/java/org/influxdb/examples) directory.

## InfluxDB Reactive Client (version 3.0+ required)

The reactive client library is based on the [RxJava](https://github.com/ReactiveX/RxJava). 
It's support all configurations from the core library packaged to the reactive API and also a lot of improvements:
- configurable backpressure
- easily writing/reading POJOs
- streamed queries 
   
### Factory

The `InfluxDBReactiveFactory` creates the reactive instance of a InfluxDB client. 
The `InfluxDBReactive` client can be configured by two parameters:
- `InfluxDBOptions` -  the configuration of connection to the InfluxDB
- `BatchOptionsReactive` - the configuration of batching

The `InfluxDBReactive` client can be also created with default batching configuration by:
```java
// Connection configuration
InfluxDBOptions options = InfluxDBOptions.builder()
    .url("http://172.17.0.2:8086")
    .username("root")
    .password("root")
    .database("reactive_measurements")
    .build();

// Reactive client
InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory.connect(options);

...

influxDBReactive.close();
```
### Events
The `InfluxDBReactive` produces events that allow user to be notified and react to this events:

- `WriteSuccessEvent` - published when arrived the success response from InfluxDB server
- `WriteErrorEvent` - published when arrived the error response from InfluxDB server
- `WritePartialEvent` - published when arrived the partial error response from InfluxDB server
- `WriteUDPEvent` - published when the data was written through UDP to InfluxDB server
- `QueryParsedResponseEvent` -  published when is parsed streamed response to query result
- `BackpressureEvent` -  published when is backpressure applied
- `UnhandledErrorEvent` -  published when occurs a unhandled exception

#### Examples

##### Handle the Success write
```java
InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory.connect(options);

influxDBReactive.listenEvents(WriteSuccessEvent.class).subscribe(event -> {

    List<Point> points = event.getPoints();

    // handle success
    ...
});
```
##### Handle the Error Write
```java
InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory.connect(options);

influxDBReactive.listenEvents(WriteErrorEvent.class).subscribe(event -> {
            
    InfluxDBException exception = event.getException();
    List<Point> points = event.getPoints();

    // handle error
    ...
});
```
    
### Writes

The writes can be configured by `WriteOptions` and are processed in batches which are configurable by `BatchOptionsReactive`.
It's use the same **Retry on error** strategy as non reactive client. 

The `InfluxDBReactive` supports write data points to InfluxDB as POJO, `org.influxdb.dto.Point` or directly in [InfluxDB Line Protocol](https://docs.influxdata.com/influxdb/latest/write_protocols/line_protocol_tutorial/).

#### Write configuration
- `database` - the name of the database to write
- `retentionPolicy` - the Retention Policy to use
- `consistencyLevel` - the ConsistencyLevel to use
- `precision` - the time precision to use
- `udp` 
    - `enable` - enable write data through [UDP](https://docs.influxdata.com/influxdb/latest/supported_protocols/udp/)
    - `port` - the UDP Port where InfluxDB is listening

```java
WriteOptions writeOptions = WriteOptions.builder()
    .database("reactive_measurements")
    .retentionPolicy("my_policy")
    .consistencyLevel(InfluxDB.ConsistencyLevel.QUORUM)
    .precision(TimeUnit.MINUTES)
    .build();

influxDBReactive.writeMeasurements(measurements, writeOptions);
```
The writes can be also used with default configuration by:
```java
influxDBReactive.writeMeasurements(measurements);
```

#### Batching configuration
- `batchSize` - the number of data point to collect in batch
- `flushInterval` - the number of milliseconds before the batch is written 
- `jitterInterval` - the number of milliseconds to increase the batch flush interval by a random amount (see documentation above)
- `retryInterval` - the number of milliseconds to retry unsuccessful write
- `bufferLimit` - the maximum number of unwritten stored points
- `writeScheduler` - the scheduler which is used for write data points (by overriding default settings can be disabled batching)
- `backpressureStrategy` - the strategy to deal with buffer overflow

```java
BatchOptionsReactive batchOptions = BatchOptionsReactive.builder()
    .batchSize(5_000)
    .flushInterval(10_000)
    .jitterInterval(5_000)
    .retryInterval(5_000)
    .bufferLimit(100_000)
    .backpressureStrategy(BackpressureOverflowStrategy.ERROR)
    .build();

// Reactive client
InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory.connect(options, batchOptions);

...

influxDBReactive.close();
```
The BatchOptionsReactive can be also created with default configuration by:
```java
// batchSize = 1_000
// flushInterval = 1_000
// jitterInterval = 0
// retryInterval = 1_000
// bufferLimit = 10_000
// writeScheduler = Schedulers.trampoline()
// backpressureStrategy = DROP_OLDEST
BatchOptions options = BatchOptions.DEFAULTS;
```
There is also configuration for disable batching (data points are written asynchronously one-by-one):
```java
BatchOptionsReactive disabledBatching = BatchOptionsReactive.DISABLED;

// Reactive client
InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory.connect(options, disabledBatching);

...

influxDBReactive.close();
```
#### Backpressure
The backpressure presents the problem of what to do with a growing backlog of unconsumed data points. 
The key feature of backpressure is provides capability to avoid consuming the unexpected amount of system resources.  
This situation is not common and can be caused by several problems: generating too much measurements in short interval,
long term unavailability of the InfluxDB server, network issues. 

The size of backlog is configured by 
`BatchOptionsReactive.bufferLimit` and backpressure strategy by `BatchOptionsReactive.backpressureStrategy`.

##### Strategy how react to backlog overflows
- `DROP_OLDEST` - Drop the oldest data points from the backlog 
- `DROP_LATEST` - Drop the latest data points from the backlog  
- `ERROR` - Signal a exception
- `BLOCK` - (not implemented yet) Wait specified time for space in buffer to become available
  - `timeout` - how long to wait before giving up
  - `unit` - TimeUnit of the timeout

If is used the strategy `DROP_OLDEST` or `DROP_LATEST` there is a possibility to react on backpressure event and slowdown the producing new measurements:
```java
InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory.connect(options, batchOptions);
influxDBReactive.listenEvents(BackpressureEvent.class).subscribe(event -> {
    
    // slowdown producers
    ...
});
```

#### Examples

##### Write POJO
```java
CpuLoad cpuLoad = new CpuLoad();
cpuLoad.host = "server02";
cpuLoad.value = 0.67D;

influxDBReactive.writeMeasurement(cpuLoad);
```

##### Write Point
```java
Point point = Point.measurement("h2o_feet")
    .tag("location", "coyote_creek")
    .addField("water_level", 2.927)
    .addField("level description", "below 3 feet")
    .time(1440046800, TimeUnit.NANOSECONDS)
    .build();

influxDBReactive.writePoint(point);
```

##### Write InfluxDB Line Protocol
```java
String record = "h2o_feet,location=coyote_creek water_level=2.927,level\\ description=\"below 3 feet\"";

influxDBReactive.writeRecord(record);
```

##### Write measurements every 10 seconds
```java
Flowable<H2OFeetMeasurement> measurements = Flowable.interval(10, TimeUnit.SECONDS, Schedulers.trampoline())
    .map(time -> {

        double h2oLevel = getLevel();
        String location = getLocation();
        String description = getLocationDescription();
                    
        return new H2OFeetMeasurement(location, h2oLevel, description, Instant.now());
    });
        
influxDBReactive.writeMeasurements(measurements);
```

##### Write through UDP
```java
WriteOptions udpOptions = WriteOptions.builder()
    .udp(true, 8089)
    .build();

CpuLoad cpuLoad = new CpuLoad();
cpuLoad.host = "server02";
cpuLoad.value = 0.67D;

influxDBReactive.writeMeasurement(cpuLoad, udpOptions);
```


### Queries
The queries uses the [InfluxDB chunking](https://docs.influxdata.com/influxdb/latest/guides/querying_data/#chunking) 
for streaming response to the consumer. The default `chunk_size` is preconfigured to 10,000 points 
(or series) and can be configured for every query by `QueryOptions`.

#### Query configuration
- `chunkSize` - the number of QueryResults to process in one chunk
- `precision` - the time unit of the results 

```java
QueryOptions options = QueryOptions.builder()
    .chunkSize(20_000)
    .precision(TimeUnit.SECONDS)
    .build();

Query query = new Query("select * from cpu", "telegraf");
Flowable<CpuMeasurement> measurements = influxDBReactive.query(query, Cpu.class, options);
...
```
#### Examples
##### The CPU usage in last 72 hours
```java
Instant last72hours = Instant.now().minus(72, ChronoUnit.HOURS);

Query query = new Query("select * from cpu", "telegraf");

Single<Double> sum = influxDBReactive.query(query, Cpu.class)
    .filter(cpu -> cpu.time.isAfter(last72hours))
    .map(cpu -> cpu.usageUser)
    .reduce(0D, (usage1, usage2) -> usage1 + usage2);

System.out.println("The CPU usage in last 72 hours: " + sum.blockingGet());
```
##### The maximum disks usages
```java
Query query = new Query("select * from disk", "telegraf");

Flowable<Disk> maximumDisksUsages = influxDBReactive.query(query, Disk.class)
    .groupBy(disk -> disk.device)
    .flatMap(group -> group
        .reduce((disk1, disk2) -> disk1.usedPercent.compareTo(disk2.usedPercent) > 0 ? disk1 : disk2)
        .toFlowable());

maximumDisksUsages.subscribe(disk -> System.out.println("Device: " + disk.device + " percent usage: " + disk.usedPercent));
```
##### Group measurements by host
```java
Flowable<Cpu> cpu = influxDBReactive.query(new Query("select * from cpu", "telegraf"), Cpu.class);
Flowable<Mem> mem = influxDBReactive.query(new Query("select * from mem", "telegraf"), Mem.class);

Flowable.merge(cpu, mem)
    .groupBy(it -> it instanceof Cpu ? ((Cpu) it).host : ((Mem) it).host)
    .flatMap(group -> {
                    
        // Operate with grouped measurements by their tag host
        
    });
```

### Flux - Influx data language
The [Flux](https://github.com/influxdata/platform/tree/master/query#flux---influx-data-language) is a Functional Language for defining a query to execute.
The `InfluxDBReactive` had to be configured by `FluxOptions` for support Flux querying.

#### Flux configuration
- `url` -  the url to connect to Flux
- `orgID` - the organization id required by Flux 

```java
FluxOptions fluxOptions = FluxOptions.builder()
    .url("http://localhost:8093")
    .orgID("0")
    .build();

// Reactive client
InfluxDBReactive influxDBReactive = InfluxDBReactiveFactory.connect(options, fluxOptions);

...

influxDBReactive.close();
```
#### Supported Functions
- [from](https://github.com/influxdata/platform/tree/master/query#from) - get data from the specified database
- [count](https://github.com/influxdata/platform/tree/master/query#count) - counts the number of results
- [first](https://github.com/influxdata/platform/tree/master/query#first) - returns the first result of the query
- [group](https://github.com/influxdata/platform/tree/master/query#group) - groups results by a user-specified set of tags
- [last](https://github.com/influxdata/platform/tree/master/query#last) - returns the last result of the query
- [limit](https://github.com/influxdata/platform/tree/master/query#limit) - restricts the number of rows returned in the results
- [max](https://github.com/influxdata/platform/tree/master/query#max) - returns the max value within the results
- [mean](https://github.com/influxdata/platform/tree/master/query#mean) - returns the mean of the values within the results
- [min](https://github.com/influxdata/platform/tree/master/query#min) - returns the min value within the results
- [range](https://github.com/influxdata/platform/tree/master/query#range) - filters the results by time boundaries
- [skew](https://github.com/influxdata/platform/tree/master/query#skew) - skew of the results
- [sort](https://github.com/influxdata/platform/tree/master/query#sort) - sorts the results by the specified columns
- [spread](https://github.com/influxdata/platform/tree/master/query#spread) - difference between min and max values
- [stddev](https://github.com/influxdata/platform/tree/master/query#stddev) - standard Deviation of the results
- [sum](https://github.com/influxdata/platform/tree/master/query#sum) - sum of the results
- [toBool](https://github.com/influxdata/platform/tree/master/query#tobool) - convert a value to a bool
- [toInt](https://github.com/influxdata/platform/tree/master/query#toint) - convert a value to a int
- [toFloat](https://github.com/influxdata/platform/tree/master/query#tofloat) - convert a value to a float
- [toDuration](https://github.com/influxdata/platform/tree/master/query#toduration) - convert a value to a duration
- [toString](https://github.com/influxdata/platform/tree/master/query#tostring) - convert a value to a string
- [toTime](https://github.com/influxdata/platform/tree/master/query#totime) - convert a value to a time
- [toUInt](https://github.com/influxdata/platform/tree/master/query#touint) - convert a value to a uint

#### Named parameters
```java
Map<String, Object> parameters = new HashMap<>();
parameters.put("limitParameter", 5);

Flux flux = Flux
    .from("telegraf")
    .limit("limitParameter");

Flowable<Cpu> cpu = influxDBReactive.flux(flux, parameters, Cpu.class);
```

#### Examples
```java
//  from(db:"telegraf")
//      |> filter(fn: (r) => r["_measurement"] == "cpu" AND r["_field"] == "usage_user")
//      |> range(start:-170h)
//      |> sum()'

Flux flux = Flux.from("telegraf")
    .filter(...)
    .range(-170L, TimeUnit.HOURS)
    .sum()
    
Flowable<Cpu> cpu = influxDBReactive.flux(flux, Cpu.class);
```
### Advanced Usage

#### Gzip's support 
Same as the non reactive client. For detail information see [documentation](#user-content-gzips-support-version-25-required).

#### Check the status and version of InfluxDB instance
The InfluxDB HTTP API [ping](https://docs.influxdata.com/influxdb/latest/tools/api/#ping) endpoint provides ability 
to check the status of your InfluxDB instance and your version of InfluxDB:

```java
// check response time and version
influxDBReactive
    .ping()
    .subscribe(pong -> {
        
        long responseTime = pong.getResponseTime();
        String version = pong.getVersion();
        
        System.out.println("InfluxDB response time: " + responseTime + " version: " + version);
    });

// check only the version
influxDBReactive
    .version()
    .subscribe(version -> System.out.println("InfluxDB version: " + version));
```



## Version

The latest version for maven dependence:
```xml
<dependency>
  <groupId>org.influxdb</groupId>
  <artifactId>influxdb-java</artifactId>
  <version>3.0</version>
</dependency>
```

Starting with version 3.0 is influxdb-java split into following modules:

* influxdb-core - core client library backward compatible with 2.X (artifactId is `influxdb-java`)
* influxdb-reactive - reactive client library based on RxJava
* influxdb-flux - support for the Flux - Functional Language for defining a query to execute
* influxdb-jmx - jmx monitoring of client performance, connection pool, number of calls

You can also add dependency `influxdb-java` to your project using BOM  "Bill Of Materials".     

```xml  
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.influxdb</groupId>
            <artifactId>influxdb-java-bom</artifactId>
            <version>3.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.influxdb</groupId>
        <artifactId>influxdb-java</artifactId>
    </dependency>
    <dependency>
        <groupId>org.influxdb</groupId>
        <artifactId>influxdb-jmx</artifactId>
    </dependency>
        <groupId>org.influxdb</groupId>
        <artifactId>influxdb-reactive</artifactId>
    </dependency>
</dependencies>        
```
  
Or when using with gradle:
```groovy
dependencyManagement {
    imports {
        mavenBom 'org.influxdb:influxdb-java-bom:3.0-SNAPSHOT'
    }
}

dependencies {
    compile "org.influxdb:influxdb-java"
}
```
For version change history have a look at [ChangeLog](https://github.com/influxdata/influxdb-java/blob/master/CHANGELOG.md).


### Build Requirements

* Java 1.8+ (tested with jdk8 and jdk9)
* Maven 3.0+ (tested with maven 3.5.0)
* Docker daemon running

Then you can build influxdb-java with all tests with:

```bash
$ mvn clean install
```

If you don't have Docker running locally, you can skip tests with -DskipTests flag set to true:

```bash
$ mvn clean install -DskipTests=true
```

If you have Docker running, but it is not at localhost (e.g. you are on a Mac and using `docker-machine`) you can set an optional environment variable `INFLUXDB_IP` to point to the correct IP address:

```bash
$ export INFLUXDB_IP=192.168.99.100
$ mvn test
```

For convenience we provide a small shell script which starts a influxdb server locally and executes `mvn clean install` with all tests inside docker containers.

```bash
$ ./compile-and-test.sh
```


### Publishing

This is a
[link](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide)
to the sonatype oss guide to publishing. I'll update this section once
the [jira ticket](https://issues.sonatype.org/browse/OSSRH-9728) is
closed and I'm able to upload artifacts to the sonatype repositories.
