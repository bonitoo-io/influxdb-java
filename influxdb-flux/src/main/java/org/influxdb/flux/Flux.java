package org.influxdb.flux;

import org.influxdb.flux.operators.AbstractParametrizedFlux;
import org.influxdb.flux.operators.CountFlux;
import org.influxdb.flux.operators.ExpressionFlux;
import org.influxdb.flux.operators.FilterFlux;
import org.influxdb.flux.operators.FirstFlux;
import org.influxdb.flux.operators.FromFlux;
import org.influxdb.flux.operators.GroupFlux;
import org.influxdb.flux.operators.LastFlux;
import org.influxdb.flux.operators.LimitFlux;
import org.influxdb.flux.operators.MaxFlux;
import org.influxdb.flux.operators.MeanFlux;
import org.influxdb.flux.operators.MinFlux;
import org.influxdb.flux.operators.RangeFlux;
import org.influxdb.flux.operators.SampleFlux;
import org.influxdb.flux.operators.SetFlux;
import org.influxdb.flux.operators.ShiftFlux;
import org.influxdb.flux.operators.SkewFlux;
import org.influxdb.flux.operators.SortFlux;
import org.influxdb.flux.operators.SpreadFlux;
import org.influxdb.flux.operators.StddevFlux;
import org.influxdb.flux.operators.SumFlux;
import org.influxdb.flux.operators.ToBoolFlux;
import org.influxdb.flux.operators.ToDurationFlux;
import org.influxdb.flux.operators.ToFloatFlux;
import org.influxdb.flux.operators.ToIntFlux;
import org.influxdb.flux.operators.ToStringFlux;
import org.influxdb.flux.operators.ToTimeFlux;
import org.influxdb.flux.operators.ToUIntFlux;
import org.influxdb.flux.operators.WindowFlux;
import org.influxdb.flux.operators.YieldFlux;
import org.influxdb.flux.operators.properties.OperatorProperties;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#basic-syntax">Flux</a> - Data Scripting Language.
 * <br>
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md">Flux Specification</a>
 *
 * <h3>The operators:</h3>
 * <ul>
 * <li>{@link FromFlux}</li>
 * <li>{@link CountFlux}</li>
 * <li>covariance - SPEC</li>
 * <li>cumulativeSum - SPEC</li>
 * <li>derivative - SPEC</li>
 * <li>difference - SPEC</li>
 * <li>distinct - SPEC</li>
 * <li>{@link FilterFlux}</li>
 * <li>{@link FirstFlux}</li>
 * <li>{@link GroupFlux}</li>
 * <li>integral - SPEC</li>
 * <li>join - UNSUPPORTED</li>
 * <li>{@link LastFlux}</li>
 * <li>{@link LimitFlux}</li>
 * <li>map - UNSUPPORTED</li>
 * <li>{@link MaxFlux}</li>
 * <li>{@link MeanFlux}</li>
 * <li>{@link MinFlux}</li>
 * <li>percentile - SPEC</li>
 * <li>{@link RangeFlux}</li>
 * <li>{@link SampleFlux}</li>
 * <li>{@link SetFlux}</li>
 * <li>{@link ShiftFlux}</li>
 * <li>{@link SkewFlux}</li>
 * <li>{@link SortFlux}</li>
 * <li>{@link SpreadFlux}</li>
 * <li>stateTracking - SPEC</li>
 * <li>{@link StddevFlux}</li>
 * <li>{@link SumFlux}</li>
 * <li>{@link ToBoolFlux}</li>
 * <li>{@link ToIntFlux}</li>
 * <li>{@link ToFloatFlux}</li>
 * <li>{@link ToDurationFlux}</li>
 * <li>{@link ToStringFlux}</li>
 * <li>{@link ToTimeFlux}</li>
 * <li>{@link ToUIntFlux}</li>
 * <li>{@link WindowFlux}</li>
 * <li>{@link YieldFlux}</li>
 * <li>toHttp - UNSUPPORTED</li>
 * <li>toKafka - UNSUPPORTED</li>
 * <li>{@link ExpressionFlux}</li>
 * </ul>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:16)
 * @since 3.0.0
 */
public abstract class Flux {

    protected OperatorProperties operatorProperties = OperatorProperties.of();

    /**
     * Get data from the specified database.
     *
     * @param db database name
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String db) {
        Preconditions.checkNonEmptyString(db, "Database name");

        return new FromFlux(db);
    }

    /**
     * Get data from the specified database.
     *
     * @param db    database name
     * @param hosts the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String db, @Nonnull final Collection<String> hosts) {
        Preconditions.checkNonEmptyString(db, "Database name");
        Objects.requireNonNull(hosts, "Hosts are required");

        return new FromFlux(db, hosts);
    }

    /**
     * Get data from the specified database.
     *
     * @param db    database name
     * @param hosts the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String db, @Nonnull final String[] hosts) {
        Preconditions.checkNonEmptyString(db, "Database name");
        Objects.requireNonNull(hosts, "Hosts are required");

        return new FromFlux(db, hosts);
    }

    /**
     * Counts the number of results.
     *
     * @return {@link CountFlux}
     */
    @Nonnull
    public CountFlux count() {
        return new CountFlux(this);
    }

    /**
     * Counts the number of results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link CountFlux}
     */
    @Nonnull
    public CountFlux count(final boolean useStartTime) {
        return new CountFlux(this)
                .withUseStartTime(useStartTime);
    }

    /**
     * Returns the first result of the query.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link FilterFlux#withRestrictions(Restrictions)}</li>
     * <li>{@link FilterFlux#withPropertyNamed(String)}</li>
     * <li>{@link FilterFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link FilterFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link FilterFlux}
     */
    @Nonnull
    public FilterFlux filter() {
        return new FilterFlux(this);
    }

    /**
     * Returns the first result of the query.
     *
     * @param restrictions filter restrictions
     * @return {@link FilterFlux}
     */
    @Nonnull
    public FilterFlux filter(@Nonnull final Restrictions restrictions) {

        Objects.requireNonNull(restrictions, "Restrictions are required");

        return new FilterFlux(this).withRestrictions(restrictions);
    }

    /**
     * Returns the first result of the query.
     *
     * @return {@link FirstFlux}
     */
    @Nonnull
    public FirstFlux first() {
        return new FirstFlux(this);
    }

    /**
     * Returns the first result of the query.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link FirstFlux}
     */
    @Nonnull
    public FirstFlux first(final boolean useStartTime) {
        return new FirstFlux(this)
                .withUseStartTime(useStartTime);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link GroupFlux#withBy(String[])}</li>
     * <li>{@link GroupFlux#withBy(Collection)}</li>
     * <li>{@link GroupFlux#withKeep(String[])}</li>
     * <li>{@link GroupFlux#withKeep(Collection)}</li>
     * <li>{@link GroupFlux#withExcept(String[])}</li>
     * <li>{@link GroupFlux#withExcept(Collection)}</li>
     * <li>{@link GroupFlux#withPropertyNamed(String)}</li>
     * <li>{@link GroupFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link GroupFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux group() {

        return new GroupFlux(this);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupBy(@Nonnull final Collection<String> groupBy) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");

        return new GroupFlux(this).withBy(groupBy);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @param keep    Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupBy(@Nonnull final Collection<String> groupBy, @Nonnull final Collection<String> keep) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withBy(groupBy).withKeep(keep);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupBy(@Nonnull final String[] groupBy) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");

        return new GroupFlux(this).withBy(groupBy);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @param keep    Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupBy(@Nonnull final String[] groupBy, @Nonnull final String[] keep) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withBy(groupBy).withKeep(keep);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupExcept(@Nonnull final Collection<String> except) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");

        return new GroupFlux(this).withExcept(except);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @param keep   Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupExcept(@Nonnull final Collection<String> except, @Nonnull final Collection<String> keep) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withExcept(except).withKeep(keep);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupExcept(@Nonnull final String[] except) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");

        return new GroupFlux(this).withExcept(except);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @param keep   Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public GroupFlux groupExcept(@Nonnull final String[] except, @Nonnull final String[] keep) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withExcept(except).withKeep(keep);
    }

    /**
     * Returns the last result of the query.
     *
     * @return {@link LastFlux}
     */
    @Nonnull
    public LastFlux last() {
        return new LastFlux(this);
    }

    /**
     * Returns the last result of the query.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link LastFlux}
     */
    @Nonnull
    public LastFlux last(final boolean useStartTime) {
        return new LastFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Restricts the number of rows returned in the results.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link LimitFlux#withN(int)}</li>
     * <li>{@link LimitFlux#withPropertyNamed(String)}</li>
     * <li>{@link LimitFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link LimitFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link LimitFlux}
     */
    @Nonnull
    public LimitFlux limit() {

        return new LimitFlux(this);
    }

    /**
     * Restricts the number of rows returned in the results.
     *
     * @param numberOfResults The number of results
     * @return {@link LimitFlux}
     */
    @Nonnull
    public LimitFlux limit(final int numberOfResults) {

        return new LimitFlux(this).withN(numberOfResults);
    }

    /**
     * Returns the max value within the results.
     *
     * @return {@link MaxFlux}
     */
    @Nonnull
    public MaxFlux max() {
        return new MaxFlux(this);
    }

    /**
     * Returns the max value within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MaxFlux}
     */
    @Nonnull
    public MaxFlux max(final boolean useStartTime) {
        return new MaxFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Returns the mean of the values within the results.
     *
     * @return {@link MeanFlux}
     */
    @Nonnull
    public MeanFlux mean() {
        return new MeanFlux(this);
    }

    /**
     * Returns the mean of the values within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MeanFlux}
     */
    @Nonnull
    public MeanFlux mean(final boolean useStartTime) {
        return new MeanFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Returns the min value within the results.
     *
     * @return {@link MinFlux}
     */
    @Nonnull
    public MinFlux min() {
        return new MinFlux(this);
    }

    /**
     * Returns the min value within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MinFlux}
     */
    @Nonnull
    public MinFlux min(final boolean useStartTime) {
        return new MinFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Filters the results by time boundaries.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link RangeFlux#withStart(Instant)}</li>
     * <li>{@link RangeFlux#withStart(Long, ChronoUnit)}</li>
     * <li>{@link RangeFlux#withStop(Instant)}</li>
     * <li>{@link RangeFlux#withStop(Long, ChronoUnit)}</li>
     * <li>{@link RangeFlux#withPropertyNamed(String)}</li>
     * <li>{@link RangeFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link RangeFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link RangeFlux}
     */
    @Nonnull
    public RangeFlux range() {

        return new RangeFlux(this);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @return {@link RangeFlux}
     */
    @Nonnull
    public RangeFlux range(@Nonnull final Instant start) {
        Objects.requireNonNull(start, "Start is required");

        return new RangeFlux(this).withStart(start);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @param stop  Specifies the exclusive newest time to be included in the results
     * @return {@link RangeFlux}
     */
    @Nonnull
    public RangeFlux range(@Nonnull final Instant start, @Nonnull final Instant stop) {
        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(stop, "Stop is required");

        return new RangeFlux(this).withStart(start).withStop(stop);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @param unit  a {@code ChronoUnit} determining how to interpret the {@code start} parameter
     * @return {@link RangeFlux}
     */
    @Nonnull
    public RangeFlux range(@Nonnull final Long start, @Nonnull final ChronoUnit unit) {
        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        return new RangeFlux(this).withStart(start, unit);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @param stop  Specifies the exclusive newest time to be included in the results
     * @param unit  a {@code ChronoUnit} determining how to interpret the {@code start} and {@code stop} parameter
     * @return {@link RangeFlux}
     */
    @Nonnull
    public RangeFlux range(@Nonnull final Long start, @Nonnull final Long stop, @Nonnull final ChronoUnit unit) {
        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(stop, "Stop is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        return new RangeFlux(this).withStart(start, unit).withStop(stop, unit);
    }

    /**
     * Sample values from a table.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link SampleFlux#withN(int)}</li>
     * <li>{@link SampleFlux#withPos(int)}</li>
     * <li>{@link SampleFlux#withPropertyNamed(String)}</li>
     * <li>{@link SampleFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link SampleFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link SampleFlux}
     */
    @Nonnull
    public SampleFlux sample() {

        return new SampleFlux(this);
    }

    /**
     * Sample values from a table.
     *
     * @param n Sample every Nth element.
     * @return {@link SampleFlux}
     */
    @Nonnull
    public SampleFlux sample(final int n) {

        return new SampleFlux(this)
                .withN(n);
    }

    /**
     * Sample values from a table.
     *
     * @param n   Sample every Nth element.
     * @param pos Position offset from start of results to begin sampling. Must be less than @{code n}.
     * @return {@link SampleFlux}
     */
    @Nonnull
    public SampleFlux sample(final int n, final int pos) {

        if (pos >= n) {
            throw new IllegalArgumentException("pos must be less than n");
        }

        return new SampleFlux(this)
                .withN(n)
                .withPos(pos);
    }

    /**
     * Assigns a static value to each record.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link SetFlux#withKeyValue(String, String)}</li>
     * <li>{@link SetFlux#withPropertyNamed(String)}</li>
     * <li>{@link SetFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link SetFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link SetFlux}
     */
    @Nonnull
    public SetFlux set() {
        return new SetFlux(this);
    }

    /**
     * Assigns a static value to each record.
     *
     * @param key   label for the column. Has to be defined.
     * @param value value for the column. Has to be defined.
     * @return {@link SetFlux}
     */
    @Nonnull
    public SetFlux set(@Nonnull final String key, @Nonnull final String value) {
        return new SetFlux(this).withKeyValue(key, value);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link ShiftFlux#withShift(Long, ChronoUnit)}</li>
     * <li>{@link ShiftFlux#withColumns(String[])}</li>
     * <li>{@link ShiftFlux#withColumns(Collection)} )}</li>
     * <li>{@link ShiftFlux#withPropertyNamed(String)}</li>
     * <li>{@link ShiftFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link ShiftFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public ShiftFlux shift() {
        return new ShiftFlux(this);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * @param amount The amount to add to each time value
     * @param unit   a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public ShiftFlux shift(@Nonnull final Long amount,
                           @Nonnull final ChronoUnit unit) {

        return new ShiftFlux(this).withShift(amount, unit);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * @param amount  The amount to add to each time value
     * @param unit    a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @param columns The list of all columns that should be shifted.
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public ShiftFlux shift(@Nonnull final Long amount,
                           @Nonnull final ChronoUnit unit,
                           @Nonnull final String[] columns) {

        return new ShiftFlux(this).withShift(amount, unit).withColumns(columns);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * @param amount  The amount to add to each time value
     * @param unit    a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @param columns The list of all columns that should be shifted.
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public ShiftFlux shift(@Nonnull final Long amount,
                           @Nonnull final ChronoUnit unit,
                           @Nonnull final Collection<String> columns) {

        return new ShiftFlux(this).withShift(amount, unit).withColumns(columns);
    }

    /**
     * Skew of the results.
     *
     * @return {@link SkewFlux}
     */
    @Nonnull
    public SkewFlux skew() {
        return new SkewFlux(this);
    }

    /**
     * Skew of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SkewFlux}
     */
    @Nonnull
    public SkewFlux skew(final boolean useStartTime) {
        return new SkewFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @return {@link SortFlux}
     */
    @Nonnull
    public SortFlux sort() {
        return new SortFlux(this);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param desc use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public SortFlux sort(final boolean desc) {
        return new SortFlux(this).withDesc(desc);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @return {@link SortFlux}
     */
    @Nonnull
    public SortFlux sort(@Nonnull final String[] columns) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this).withCols(columns);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @return {@link SortFlux}
     */
    @Nonnull
    public SortFlux sort(@Nonnull final Collection<String> columns) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this).withCols(columns);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @param desc    use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public SortFlux sort(@Nonnull final String[] columns, final boolean desc) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this)
                .withCols(columns)
                .withDesc(desc);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @param desc    use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public SortFlux sort(@Nonnull final Collection<String> columns, final boolean desc) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this)
                .withCols(columns)
                .withDesc(desc);
    }

    /**
     * Difference between min and max values.
     *
     * @return {@link SpreadFlux}
     */
    @Nonnull
    public SpreadFlux spread() {
        return new SpreadFlux(this);
    }

    /**
     * Difference between min and max values.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SpreadFlux}
     */
    @Nonnull
    public SpreadFlux spread(final boolean useStartTime) {
        return new SpreadFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Standard Deviation of the results.
     *
     * @return {@link StddevFlux}
     */
    @Nonnull
    public StddevFlux stddev() {
        return new StddevFlux(this);
    }

    /**
     * Standard Deviation of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link StddevFlux}
     */
    @Nonnull
    public StddevFlux stddev(final boolean useStartTime) {
        return new StddevFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Sum of the results.
     *
     * @return {@link SumFlux}
     */
    @Nonnull
    public SumFlux sum() {
        return new SumFlux(this);
    }

    /**
     * Sum of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SumFlux}
     */
    @Nonnull
    public SumFlux sum(final boolean useStartTime) {
        return new SumFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Convert a value to a bool.
     *
     * @return {@link ToBoolFlux}
     */
    @Nonnull
    public ToBoolFlux toBool() {
        return new ToBoolFlux(this);
    }

    /**
     * Convert a value to a int.
     *
     * @return {@link ToIntFlux}
     */
    @Nonnull
    public ToIntFlux toInt() {
        return new ToIntFlux(this);
    }

    /**
     * Convert a value to a float.
     *
     * @return {@link ToFloatFlux}
     */
    @Nonnull
    public ToFloatFlux toFloat() {
        return new ToFloatFlux(this);
    }

    /**
     * Convert a value to a duration.
     *
     * @return {@link ToDurationFlux}
     */
    @Nonnull
    public ToDurationFlux toDuration() {
        return new ToDurationFlux(this);
    }

    /**
     * Convert a value to a string.
     *
     * @return {@link ToStringFlux}
     */
    @Nonnull
    public ToStringFlux toStringConvert() {
        return new ToStringFlux(this);
    }

    /**
     * Convert a value to a time.
     *
     * @return {@link ToTimeFlux}
     */
    @Nonnull
    public ToTimeFlux toTime() {
        return new ToTimeFlux(this);
    }

    /**
     * Convert a value to a uint.
     *
     * @return {@link ToUIntFlux}
     */
    @Nonnull
    public ToUIntFlux toUInt() {
        return new ToUIntFlux(this);
    }

    /**
     * Partitions the results by a given time range.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link WindowFlux#withEvery(Long, ChronoUnit)}</li>
     * <li>{@link WindowFlux#withPeriod(Long, ChronoUnit)}</li>
     * <li>{@link WindowFlux#withStart(Long, ChronoUnit)}</li>
     * <li>{@link WindowFlux#withStart(Instant)}</li>
     * <li>{@link WindowFlux#withRound(Long, ChronoUnit)}</li>
     * <li>{@link WindowFlux#withColumn(String)}</li>
     * <li>{@link WindowFlux#withStartCol(String)}</li>
     * <li>{@link WindowFlux#withStartCol(String)}</li>
     * <li>{@link WindowFlux#withPropertyNamed(String)}</li>
     * <li>{@link WindowFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link WindowFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window() {
        return new WindowFlux(this);
    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every     duration of time between windows
     * @param everyUnit a {@code ChronoUnit} determining how to interpret the {@code every}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit) {

        Objects.requireNonNull(every, "Every is required");
        Objects.requireNonNull(everyUnit, "Every ChronoUnit is required");

        return new WindowFlux(this).withEvery(every, everyUnit);
    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit,
                             @Nonnull final Long period,
                             @Nonnull final ChronoUnit periodUnit) {

        Objects.requireNonNull(every, "Every is required");
        Objects.requireNonNull(everyUnit, "Every ChronoUnit is required");

        Objects.requireNonNull(period, "Period is required");
        Objects.requireNonNull(periodUnit, "Period ChronoUnit is required");

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit);
    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param start      the time of the initial window partition
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit,
                             @Nonnull final Long period,
                             @Nonnull final ChronoUnit periodUnit,
                             @Nonnull final Instant start) {

        Objects.requireNonNull(every, "Every is required");
        Objects.requireNonNull(everyUnit, "Every ChronoUnit is required");

        Objects.requireNonNull(period, "Period is required");
        Objects.requireNonNull(periodUnit, "Period ChronoUnit is required");

        Objects.requireNonNull(start, "Start is required");

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withStart(start);
    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param start      the time of the initial window partition
     * @param startUnit  a {@code ChronoUnit} determining how to interpret the {@code start}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit,
                             @Nonnull final Long period,
                             @Nonnull final ChronoUnit periodUnit,
                             @Nonnull final Long start,
                             @Nonnull final ChronoUnit startUnit) {

        Objects.requireNonNull(every, "Every is required");
        Objects.requireNonNull(everyUnit, "Every ChronoUnit is required");

        Objects.requireNonNull(period, "Period is required");
        Objects.requireNonNull(periodUnit, "Period ChronoUnit is required");

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(startUnit, "Start ChronoUnit is required");

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withStart(start, startUnit);
    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param start      the time of the initial window partition
     * @param startUnit  a {@code ChronoUnit} determining how to interpret the {@code start}
     * @param round      rounds a window's bounds to the nearest duration
     * @param roundUnit  a {@code ChronoUnit} determining how to interpret the {@code round}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit,
                             @Nonnull final Long period,
                             @Nonnull final ChronoUnit periodUnit,
                             @Nonnull final Long start,
                             @Nonnull final ChronoUnit startUnit,
                             @Nonnull final Long round,
                             @Nonnull final ChronoUnit roundUnit) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withStart(start, startUnit)
                .withRound(round, roundUnit);

    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param start      the time of the initial window partition
     * @param round      rounds a window's bounds to the nearest duration
     * @param roundUnit  a {@code ChronoUnit} determining how to interpret the {@code round}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit,
                             @Nonnull final Long period,
                             @Nonnull final ChronoUnit periodUnit,
                             @Nonnull final Instant start,
                             @Nonnull final Long round,
                             @Nonnull final ChronoUnit roundUnit) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withStart(start)
                .withRound(round, roundUnit);

    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param start      the time of the initial window partition
     * @param startUnit  a {@code ChronoUnit} determining how to interpret the {@code start}
     * @param round      rounds a window's bounds to the nearest duration
     * @param roundUnit  a {@code ChronoUnit} determining how to interpret the {@code round}
     * @param timeColumn name of the time column to use
     * @param startCol   name of the column containing the window start time
     * @param stopCol    name of the column containing the window stop time
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit,
                             @Nonnull final Long period,
                             @Nonnull final ChronoUnit periodUnit,
                             @Nonnull final Long start,
                             @Nonnull final ChronoUnit startUnit,
                             @Nonnull final Long round,
                             @Nonnull final ChronoUnit roundUnit,
                             @Nonnull final String timeColumn,
                             @Nonnull final String startCol,
                             @Nonnull final String stopCol) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withStart(start, startUnit)
                .withRound(round, roundUnit)
                .withColumn(timeColumn)
                .withStartCol(startCol)
                .withStopCol(stopCol);

    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param start      the time of the initial window partition
     * @param round      rounds a window's bounds to the nearest duration
     * @param roundUnit  a {@code ChronoUnit} determining how to interpret the {@code round}
     * @param timeColumn name of the time column to use
     * @param startCol   name of the column containing the window start time
     * @param stopCol    name of the column containing the window stop time
     * @return {@link WindowFlux}
     */
    @Nonnull
    public WindowFlux window(@Nonnull final Long every,
                             @Nonnull final ChronoUnit everyUnit,
                             @Nonnull final Long period,
                             @Nonnull final ChronoUnit periodUnit,
                             @Nonnull final Instant start,
                             @Nonnull final Long round,
                             @Nonnull final ChronoUnit roundUnit,
                             @Nonnull final String timeColumn,
                             @Nonnull final String startCol,
                             @Nonnull final String stopCol) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withStart(start)
                .withRound(round, roundUnit)
                .withColumn(timeColumn)
                .withStartCol(startCol)
                .withStopCol(stopCol);
    }

    /**
     * Yield a query results to yielded results.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link YieldFlux#withName(String)}</li>
     * <li>{@link YieldFlux#withPropertyNamed(String)}</li>
     * <li>{@link YieldFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link YieldFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link YieldFlux}
     */
    @Nonnull
    public YieldFlux yield() {
        return new YieldFlux(this);
    }

    /**
     * Yield a query results to yielded results.
     *
     * @param name The unique name to give to yielded results. Has to be defined.
     * @return {@link YieldFlux}
     */
    @Nonnull
    public YieldFlux yield(@Nonnull final String name) {
        return new YieldFlux(this).withName(name);
    }

    /**
     * Write the custom Flux expression.
     *
     * @param expression flux expression
     * @return {@link ExpressionFlux}
     */
    @Nonnull
    public ExpressionFlux expression(@Nonnull final String expression) {

        Preconditions.checkNonEmptyString(expression, "Expression");

        return new ExpressionFlux(this, expression);
    }

    /**
     * Create new operator with type {@code type}.
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .operator(FilterMeasurement.class)
     *          .withName("cpu")
     *      .sum();
     * </pre>
     *
     * @param type operator type
     * @param <F>  operator type
     * @return operator with {@code type}
     */
    @Nonnull
    public <F extends AbstractParametrizedFlux> F operator(@Nonnull final Class<F> type) {

        Objects.requireNonNull(type, "Operator type is required");

        try {
            return type.getConstructor(Flux.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add named property to current operator.
     *
     * <pre>
     *  FluxChain fluxChain = new FluxChain()
     *      .withPropertyNamed("every", 15, ChronoUnit.MINUTES)
     *      .withPropertyNamed("period", 20L, ChronoUnit.SECONDS)
     *      .withPropertyNamed("start", -50, ChronoUnit.DAYS)
     *      .withPropertyNamed("round", 1L, ChronoUnit.HOURS);
     *
     *  Flux flux = Flux.from("telegraf")
     *      .window()
     *          .withPropertyNamed("every")
     *          .withPropertyNamed("period")
     *          .withPropertyNamed("start")
     *          .withPropertyNamed("round")
     *      .sum();
     *
     * flux.print(fluxChain);
     * </pre>
     *
     * @param property name in Flux query and in named properties
     * @return a current operator.
     */
    @Nonnull
    public Flux withPropertyNamed(@Nonnull final String property) {
        return withPropertyNamed(property, property);
    }

    /**
     * Add named property to current operator.
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .limit()
     *          .withPropertyNamed("n", "limit")
     *      .sum();
     *
     * FluxChain fluxChain = new FluxChain()
     *      .withPropertyNamed("limit", 15);
     *
     * flux.print(fluxChain);
     * </pre>
     *
     * @param fluxName      name in Flux query
     * @param namedProperty name in named properties
     * @return a current operator
     */
    @Nonnull
    public Flux withPropertyNamed(@Nonnull final String fluxName, @Nonnull final String namedProperty) {

        Preconditions.checkNonEmptyString(fluxName, "Flux property name");
        Preconditions.checkNonEmptyString(namedProperty, "Named property");

        this.operatorProperties.putPropertyNamed(fluxName, namedProperty);

        return this;
    }

    /**
     * Add property value to current operator.
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .limit()
     *          .withPropertyValue("n", 5)
     *      .sum();
     * </pre>
     *
     * @param propertyName name in Flux query
     * @param value        value of property. If null than ignored.
     * @return a current operator
     */
    @Nonnull
    public Flux withPropertyValue(@Nonnull final String propertyName, @Nullable final Object value) {

        Preconditions.checkNonEmptyString(propertyName, "Flux property name");

        this.operatorProperties.putPropertyValue(propertyName, value);

        return this;
    }

    /**
     * Add string property value to current operator that will be quoted (value =&gt; "value").
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .window(5, ChronoUnit.MINUTES)
     *          .withPropertyValueEscaped("startCol", "differentCol")
     *      .sum();
     * </pre>
     *
     * @param property name of property in Flux query
     * @param amount   the amount of the duration, measured in terms of the unit, positive or negative
     * @param unit     the unit that the duration is measured in, must have an exact duration.  If null than ignored.
     * @return a current operator
     */
    @Nonnull
    public Flux withPropertyValue(@Nonnull final String property, final long amount, @Nonnull final ChronoUnit unit) {

        Preconditions.checkNonEmptyString(property, "Flux property name");

        this.operatorProperties.putPropertyValue(property, amount, unit);

        return this;
    }

    /**
     * Add string property value to current operator that will be quoted (value =&gt; "value").
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .window(5, ChronoUnit.MINUTES)
     *          .withPropertyValueEscaped("startCol", "differentCol")
     *      .sum();
     * </pre>
     *
     * @param property name of property in Flux query
     * @param value    value of property. If null than ignored.
     * @return a current operator
     */
    @Nonnull
    public Flux withPropertyValueEscaped(@Nonnull final String property, @Nullable final String value) {

        Preconditions.checkNonEmptyString(property, "Flux property name");

        this.operatorProperties.putPropertyValueString(property, value);

        return this;
    }

    /**
     * Append the actual operator to {@link FluxChain}.
     *
     * @param fluxChain the incoming {@link FluxChain}, never null
     */
    protected abstract void appendActual(@Nonnull final FluxChain fluxChain);

    /**
     * Create the Flux script that can be executed in {@code fluxd}.
     *
     * @return Flux script
     */
    @Nonnull
    public String print() {
        return print(new FluxChain());
    }

    /**
     * Create the Flux script that can be executed in {@code fluxd}.
     *
     * @param fluxChain parameter source
     * @return Flux script
     */
    @Nonnull
    public String print(@Nonnull final FluxChain fluxChain) {

        Objects.requireNonNull(fluxChain, "FluxChain is required");

        appendActual(fluxChain);
        return fluxChain.print();
    }

    @Override
    public String toString() {
        return print();
    }
}
