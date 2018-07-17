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

## Flux - Data Scripting Language (version 3.0+ required)
The [Flux](https://github.com/influxdata/platform/tree/master/query#flux---influx-data-language) is a Functional Language for defining a query to execute.
The `FluxReactive` is reactive client that support the Flux Language.

### Factory

The `FluxReactiveFactory` creates the reactive instance of a Flux client. The `FluxReactive` client can be configured by `FluxOptions`.

#### Flux configuration
- `url` -  the url to connect to Flux
- `orgID` - the organization id required by Flux 
- `okHttpClient` - the HTTP client to use for communication with Flux (optional)

```java
// Connection configuration
FluxOptions fluxOptions = FluxOptions.builder()
    .url("http://localhost:8093")
    .orgID("0")
    .build();

// Reactive client
FluxReactive fluxReactive = FluxReactiveFactory.connect(fluxOptions);

...

fluxReactive.close();
```

### Flux query configuration

The Flux query can be configured by `FluxQueryOptions`:

- `parserOptions` - the CSV parser options
    - `valueDestinations` - the column names of the record where result will be placed (see [map function](#map))
    
```java
FluxCsvParserOptions parserOptions = FluxCsvParserOptions.builder()
    .valueDestinations("value1", "_value2", "value_str")
    .build();

FluxQueryOptions queryOptions = FluxQueryOptions.builder()
    .parserOptions(parserOptions)
    .build();

Flowable<FluxResult> results = fluxReactive.flux(Flux.from("telegraf"), queryOptions);
```

### Events
The `FluxReactive` produces events that allow user to be notified and react to this events:

- `FluxSuccessEvent` - published when arrived the success response from Flux server
- `FluxErrorEvent` - published when arrived the error response from Flux server

#### Handling success response
```java
FluxReactive fluxReactive = FluxReactiveFactory.connect(fluxOptions);
fluxReactive.listenEvents(FluxSuccessEvent.class).subscribe(event -> {

    // handle success
    
    String query = event.getFluxQuery();
    ...
});
```
#### Handling error response
```java
FluxReactive fluxReactive = FluxReactiveFactory.connect(fluxOptions);
fluxReactive.listenEvents(FluxErrorEvent.class).subscribe(event -> {
    
    // handle error
    
    InfluxDBException influxDBException = event.getException();
    ...
});
```

### Functions properties
There are four possibilities how to add properties to the functions.

#### Use build-in constructor
```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS)
    .sum();

Flowable<FluxResult> results = fluxReactive.flux(flux);
```
#### Use build-in properties
```java
Flux.from("telegraf")
    .window()
        .withEvery(15L, ChronoUnit.MINUTES)
        .withPeriod(20L, ChronoUnit.SECONDS)
    .sum();

Flowable<FluxResult> results = fluxReactive.flux(flux);
```

#### Specify the property name and value
```java
Flux.from("telegraf")
    .window()
        .withPropertyValue("every", 15L, ChronoUnit.MINUTES)
        .withPropertyValue("period", 20L, ChronoUnit.SECONDS)
    .sum();

Flowable<FluxResult> results = fluxReactive.flux(flux);
```
#### Specify the named properties
```java
Map<String, Object> properties = new HashMap<>();
properties.put("every", new TimeInterval(15L, ChronoUnit.MINUTES));
properties.put("period", new TimeInterval(20L, ChronoUnit.SECONDS));

Flux flux = Flux
    .from("telegraf")
    .window()
        .withPropertyNamed("every")
        .withPropertyNamed("period")
    .sum();

Flowable<FluxResult> cpu = fluxReactive.flux(flux, properties);
```

### Supported Functions

#### from

Starting point for all queries. Get data from the specified database [[doc](https://github.com/influxdata/platform/tree/master/query#from)].
- `db` - The name of the database to query [string].
- `hosts` - [array of strings]

```java
Flux flux = Flux.from("telegraf");
```
```java
Flux flux = Flux
    .from("telegraf", new String[]{"192.168.1.200", "192.168.1.100"})
    .last();
```

#### count
Counts the number of results [[doc](https://github.com/influxdata/platform/tree/master/query#count)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

#### derivative
Computes the time based difference between subsequent non null records [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#derivative)].
- `unit` - The time duration to use for the result [duration].
- `nonNegative` - Indicates if the derivative is allowed to be negative [boolean].
- `columns` - List of columns on which to compute the derivative [array of strings].
- `timeSrc` - The source column for the time values. Defaults to `_time` [string].

```java
Flux flux = Flux
    .from("telegraf")
    .derivative(1L, ChronoUnit.MINUTES);
```

```java
Flux flux = Flux
    .from("telegraf")
    .derivative()
        .withUnit(10L, ChronoUnit.DAYS)
        .withNonNegative(true)
        .withColumns(new String[]{"columnCompare_1", "columnCompare_2"})
        .withTimeSrc("_timeColumn");
```

#### difference
Difference computes the difference between subsequent non null records [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#difference)].
- `nonNegative` - Indicates if the derivative is allowed to be negative. If a value is encountered which is less than the previous value then it is assumed the previous value should have been a zero [boolean].
- `columns` -  The list of columns on which to compute the difference. Defaults `["_value"]` [array of strings].
```java
Flux flux = Flux
    .from("telegraf")
    .groupBy("_measurement")
    .difference();
```
```java
Flux flux = Flux
    .from("telegraf")
    .range(-5L, ChronoUnit.MINUTES)
    .difference(new String[]{"_value", "_time"}, false;
```

#### distinct
Distinct produces the unique values for a given column [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#distinct)].
- `column` - The column on which to track unique values [string].
```java
Flux flux = Flux
    .from("telegraf")
    .groupBy("_measurement")
    .distinct("_measurement");
```

#### filter

Filters the results using an expression [[doc](https://github.com/influxdata/platform/tree/master/query#filter)].
- `fn` - Function to when filtering the records. The function must accept a single parameter which will be 
the records and return a boolean value. Records which evaluate to true, will be included in the results [function(record) bool].

Supported Record columns:
- `_measurement`
- `_field`
- `_start`
- `_stop`
- `_time`
- `_value`
- `custom` - the custom column value by `Restrictions.column("_id").notEqual(5)`

Supported Record restrictions:
- `equal`
- `notEqual`
- `less`
- `greater`
- `greater`
- `lessOrEqual`
- `greaterOrEqual`
- `custom` - the custom restriction by `Restrictions.value().custom(15L, "=~")`

```java
Restrictions restrictions = Restrictions.and(
    Restrictions.measurement().equal("mem"),
    Restrictions.field().equal("usage_system"),
    Restrictions.tag("service").equal("app-server")
);

Flux flux = Flux
    .from("telegraf")
    .filter(restrictions)
    .range(-4L, ChronoUnit.HOURS)
    .count();
```
```java
Restrictions restriction = Restrictions.and(
    Restrictions.tag("instance_type").equal(Pattern.compile("/prod/")),
    Restrictions.field().greater(10.5D),
    Restrictions.time().lessOrEqual(new TimeInterval(-15L, ChronoUnit.HOURS))
);

Flux flux = Flux
    .from("telegraf")
    .filter(restriction)
    .range(-4L, 2L, ChronoUnit.HOURS)
    .count();
```

#### first
Returns the first result of the query [[doc](https://github.com/influxdata/platform/tree/master/query#first)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .first();
```

#### group
Groups results by a user-specified set of tags [[doc](https://github.com/influxdata/platform/tree/master/query#group)].
- `by` - Group by these specific tag names. Cannot be used with `except` option [array of strings].
- `keep` - Keep specific tag keys that were not in `by` in the results [array of strings].
- `except` - Group by all but these tag keys. Cannot be used with `by` option [array of strings].

```java
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupBy(new String[]{"tag_a", "tag_b"});
```
```java
// by + keep
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupBy(new String[]{"tag_a", "tag_b"}, new String[]{"tag_c"});
```
```java
// except + keep
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupExcept(new String[]{"tag_a"}, new String[]{"tag_b", "tag_c"});
```

### integral
For each aggregate column, it outputs the area under the curve of non null records. 
The curve is defined as function where the domain is the record times and the range is the record values. [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#integral)].
- `unit` - Time duration to use when computing the integral [duration].
```java
Flux flux = Flux
    .from("telegraf")
    .integral(1L, ChronoUnit.MINUTES);
```

#### last
Returns the last result of the query [[doc](https://github.com/influxdata/platform/tree/master/query#last)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .last();
```

#### limit
Restricts the number of rows returned in the results [[doc](https://github.com/influxdata/platform/tree/master/query#limit)].
- `n` - The maximum number of records to output [int]. 
```java
Flux flux = Flux
    .from("telegraf")
    .limit(5);
```
#### map
Applies a function to each row of the table [[doc](https://github.com/influxdata/platform/tree/master/query#map)].
- `fn` - The function to apply to each row. The return value of the function may be a single value or an object.

```java
// Square the value

Restrictions restriction = Restrictions.and(
    Restrictions.measurement().equal("cpu"),
    Restrictions.field().equal("usage_system"),
    Restrictions.tag("service").equal("app-server")
);

Flux flux = Flux
    .from("telegraf")
    .filter(restriction)
    .range(-12L, ChronoUnit.HOURS)
    .map("r._value * r._value");
```

```java
// Square the value and keep the original value

Restrictions restriction = Restrictions.and(
    Restrictions.measurement().equal("cpu"),
    Restrictions.field().equal("usage_system"),
    Restrictions.tag("service").equal("app-server")
);

Flux flux = Flux
    .from("telegraf")
    .filter(restriction)
    .range(-12L, ChronoUnit.HOURS)
    .map("{value: r._value, value2:r._value * r._value}");
```

#### max
Returns the max value within the results [[doc](https://github.com/influxdata/platform/tree/master/query#max)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .max();
```

#### mean
Returns the mean of the values within the results [[doc](https://github.com/influxdata/platform/tree/master/query#mean)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .mean();
```

#### min
Returns the min value within the results [[doc](https://github.com/influxdata/platform/tree/master/query#min)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .min();
```

#### range
Filters the results by time boundaries [[doc](https://github.com/influxdata/platform/tree/master/query#range)].
- `start` - Specifies the oldest time to be included in the results [duration or timestamp].
- `stop` - Specifies the exclusive newest time to be included in the results. Defaults to `"now"` [duration or timestamp].

```java
// by interval
Flux flux = Flux
    .from("telegraf")
    .range(-12L, -1L, ChronoUnit.HOURS)
```
```java
// by Instant
Flux flux = Flux
    .from("telegraf")
    .range(Instant.now().minus(4, ChronoUnit.HOURS),
           Instant.now().minus(15, ChronoUnit.MINUTES)
    );
```

#### sample
Sample values from a table [[doc](https://github.com/influxdata/platform/tree/master/query#sample)].
- `n` - Sample every Nth element [int].
- `pos` - Position offset from start of results to begin sampling. `pos` must be less than `n`. If `pos` less than 0, a random offset is used. Default is -1 (random offset) [int].
```java
Flux flux = Flux.from("telegraf")
    .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
    .range(-1L, ChronoUnit.DAYS)
    .sample(10);
```
```java
Flux flux = Flux.from("telegraf")
    .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
    .range(-1L, ChronoUnit.DAYS)
    .sample(5, 1);
```

#### set
Assigns a static value to each record [[doc](https://github.com/influxdata/platform/tree/master/query#set)].
- `key` - Label for the column to set [string].
- `value` - Value for the column to set [string].

```java
Flux flux = Flux
    .from("telegraf")
    .set("location", "Carolina");
```

#### shift
Shift add a fixed duration to time columns [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#shift)].
- `shift` - The amount to add to each time value [duration].
- `columns` - The list of all columns that should be shifted. Defaults `["_start", "_stop", "_time"]` [array of strings].
```java
Flux flux = Flux
    .from("telegraf")
    .shift(10L, ChronoUnit.HOURS);
```
```java
Flux flux = Flux
    .from("telegraf")
    .shift(10L, ChronoUnit.HOURS, new String[]{"_time", "custom"});
```

#### skew
Skew of the results [[doc](https://github.com/influxdata/platform/tree/master/query#skew)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .range(-30L, -15L, ChronoUnit.MINUTES)
    .skew();
```

#### sort
Sorts the results by the specified columns. Default sort is ascending [[doc](https://github.com/influxdata/platform/tree/master/query#skew)].
- `cols` - List of columns used to sort. Precedence from left to right. Default is `"value"` [array of strings].
- `desc` - Sort results descending. Default false [bool].

```java
Flux flux = Flux
    .from("telegraf")
    .sort(new String[]{"region", "value"});
```
```java
 Flux flux = Flux
    .from("telegraf")
    .sort(true);
```

#### spread
Difference between min and max values [[doc](https://github.com/influxdata/platform/tree/master/query#spread)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .spread();
```

#### stddev
Standard Deviation of the results [[doc](https://github.com/influxdata/platform/tree/master/query#stddev)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate [boolean].

```java
Flux flux = Flux
    .from("telegraf")
    .stddev();
```

#### sum
Sum of the results [[doc](https://github.com/influxdata/platform/tree/master/query#sum)].
```java
Flux flux = Flux
    .from("telegraf")
    .sum();
```

#### toBool
Convert a value to a bool [[doc](https://github.com/influxdata/platform/tree/master/query#tobool)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toBool();
```

#### toInt
Convert a value to a int [[doc](https://github.com/influxdata/platform/tree/master/query#toint)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toInt();
```

#### toFloat
Convert a value to a float [[doc](https://github.com/influxdata/platform/tree/master/query#tofloat)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toFloat();
```

#### toDuration
Convert a value to a duration [[doc](https://github.com/influxdata/platform/tree/master/query#toduration)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toDuration();
```

#### toString
Convert a value to a string [[doc](https://github.com/influxdata/platform/tree/master/query#tostring)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toString();
```

#### toTime
Convert a value to a time [[doc](https://github.com/influxdata/platform/tree/master/query#totime)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toTime();
```

#### toUInt
Convert a value to a uint [[doc](https://github.com/influxdata/platform/tree/master/query#touint)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toUInt();
```

#### window
Groups the results by a given time range [[doc](https://github.com/influxdata/platform/tree/master/query#window)].
- `every` - Duration of time between windows. Defaults to `period's` value [duration]. 
- `period` - Duration of the windowed partition. Defaults to `period's` value [duration]. 
- `start` - The time of the initial window partition [time].
- `round` - Rounds a window's bounds to the nearest duration. Defaults to `every's` value [duration].
- `column` - Name of the time column to use. Defaults to `_time` [string].
- `startCol` - Name of the column containing the window start time. Defaults to `_start` [string].
- `stopCol` - Name of the column containing the window stop time. Defaults to `_stop` [string].

```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES)
    .max();
```
```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES,
            20L, ChronoUnit.SECONDS,
            -50L, ChronoUnit.WEEKS,
            1L, ChronoUnit.SECONDS)
    .max();
```

#### yield
Yield a query results to yielded results [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#yield)].
- `name` - The unique name to give to yielded results [string].
```java
Flux flux = Flux
    .from("telegraf")
    .yield("0");
```

#### Custom functions
We assume that exist custom function measurement that filter measurement by their name. The `Flux` implementation looks like this: 

```flux
// Define measurement function which accepts table as the piped argument.
measurement = (m, table=<-) => table |> filter(fn: (r) => r._measurement == m)
```

The Java implementation:
```java
public class FilterMeasurement extends AbstractParametrizedFlux {

    public FilterMeasurement(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        // name of the Flux function
        return "measurement";
    }

    /**
     * @param measurement the measurement name. Has to be defined.
     * @return this
     */
    @Nonnull
    public FilterMeasurement withName(@Nonnull final String measurement) {

        Preconditions.checkNonEmptyString(measurement, "Measurement name");

        // name of parameter from the Flux function
        withPropertyValueEscaped("m", measurement);

        return this;
    }
}
```

Using the measurement function:
```java
Flux flux = Flux
    .from("telegraf")
    .operator(FilterMeasurement.class)
        .withName("cpu")
    .sum();
```

#### Custom expressions
```java
Flux flux = Flux.from("telegraf")
    .expression("map(fn: (r) => r._value * r._value)")
    .expression("sum()");

Flowable<FluxResult> results = fluxReactive.flux(flux);
```

### Mapping to POJO
Suppose that we want map the Flux response to the measurement _Memory_.

The Flux response:
```
#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string
#partition,false,false,false,false,false,false,false,false,false,true
#default,_result,,,,,,,,,
,result,table,_start,_stop,_time,_value,_field,_measurement,host,region
,,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,west
,,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,20,free,mem,B,west
,,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,11,free,mem,A,west
,,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,22,free,mem,B,west
```

The measurement Memory:
```java
@Measurement(name = "mem")
public static class Memory {
    
    @Column(name = "time")
    private Instant time;

    @Column(name = "free")
    private Long free;

    @Column(name = "host", tag = true)
    private String host;

    @Column(name = "region", tag = true)
    private String region;
}
```

The corresponding query:
```java
Flux flux = Flux.from("telegraf")
    .range(Instant.EPOCH)
    .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
    .window(10L, ChronoUnit.SECONDS)
    .groupBy("region");    
    
Flowable<Cpu> cpu = fluxReactive.flux(flux, Memory.class);
```

### Advanced Usage

#### Gzip's support 
Same as the non reactive client. For detail information see [documentation](#user-content-gzips-support-version-25-required).

#### Log HTTP Request and Response
The Requests and Responses can be logged by changing OkHttp LogLevel.
```java
fluxReactive.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);
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
* influxdb-flux - support for the Flux - Data Scripting Language
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
