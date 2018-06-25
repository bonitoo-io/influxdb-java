package org.influxdb.flux;

import org.influxdb.flux.operators.CountFlux;
import org.influxdb.flux.operators.FirstFlux;
import org.influxdb.flux.operators.FromFlux;
import org.influxdb.flux.operators.GroupFlux;
import org.influxdb.flux.operators.LastFlux;
import org.influxdb.flux.operators.LimitFlux;
import org.influxdb.flux.operators.MaxFlux;
import org.influxdb.flux.operators.MeanFlux;
import org.influxdb.flux.operators.MinFlux;
import org.influxdb.flux.operators.SkewFlux;
import org.influxdb.flux.operators.SortFlux;
import org.influxdb.flux.operators.SpreadFlux;
import org.influxdb.flux.operators.StddevFlux;
import org.influxdb.flux.operators.SumFlux;
import org.influxdb.flux.operators.ToBoolFlux;
import org.influxdb.flux.operators.ToFloatFlux;
import org.influxdb.flux.operators.ToIntFlux;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#basic-syntax">Flux</a> -
 * Functional Language for defining a query to execute.
 * <h3>The operators:</h3>
 * <ul>
 * <li>{@link FromFlux}</li>
 * <li>{@link CountFlux}</li>
 * <li>filter - UNSUPPORTED</li>
 * <li>{@link FirstFlux}</li>
 * <li>{@link GroupFlux}</li>
 * <li>join - UNSUPPORTED</li>
 * <li>{@link LastFlux}</li>
 * <li>{@link LimitFlux}</li>
 * <li>map - UNSUPPORTED</li>
 * <li>{@link MaxFlux}</li>
 * <li>{@link MeanFlux}</li>
 * <li>{@link MinFlux}</li>
 * <li>range - UNSUPPORTED</li>
 * <li>sample - UNSUPPORTED</li>
 * <li>set - UNSUPPORTED</li>
 * <li>{@link SkewFlux}</li>
 * <li>{@link SortFlux}</li>
 * <li>{@link SpreadFlux}</li>
 * <li>{@link StddevFlux}</li>
 * <li>{@link SumFlux}</li>
 * <li>{@link ToBoolFlux}</li>
 * <li>{@link ToIntFlux}</li>
 * <li>{@link ToFloatFlux}</li>
 * <li>toDuration - UNSUPPORTED</li>
 * <li>toString - UNSUPPORTED</li>
 * <li>toTime - UNSUPPORTED</li>
 * <li>toUInt - UNSUPPORTED</li>
 * <li>toUInt - UNSUPPORTED</li>
 * <li>window - UNSUPPORTED</li>
 * <li>toHttp - UNSUPPORTED</li>
 * <li>toKafka - UNSUPPORTED</li>
 * <li>byString - UNSUPPORTED</li>
 * <li>byInstance - UNSUPPORTED</li>
 * </ul>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:16)
 * @since 3.0.0
 */
public abstract class Flux {

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
    public Flux count() {
        return new CountFlux(this);
    }

    /**
     * Counts the number of results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link CountFlux}
     */
    @Nonnull
    public Flux count(final boolean useStartTime) {
        return new CountFlux(this, useStartTime);
    }

    /**
     * Counts the number of results.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link CountFlux}
     */
    @Nonnull
    public Flux count(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new CountFlux(this, useStartTimeParameterName);
    }

    /**
     * Returns the first result of the query.
     *
     * @return {@link FirstFlux}
     */
    @Nonnull
    public Flux first() {
        return new FirstFlux(this);
    }

    /**
     * Returns the first result of the query.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link FirstFlux}
     */
    @Nonnull
    public Flux first(final boolean useStartTime) {
        return new FirstFlux(this, useStartTime);
    }

    /**
     * Returns the first result of the query.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link FirstFlux}
     */
    @Nonnull
    public Flux first(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new FirstFlux(this, useStartTimeParameterName);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupBy(@Nonnull final Collection<String> groupBy) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");

        return new GroupFlux(this, groupBy, GroupFlux.GroupType.GROUP_BY);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @param keep    Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupBy(@Nonnull final Collection<String> groupBy, @Nonnull final Collection<String> keep) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this, groupBy, keep, GroupFlux.GroupType.GROUP_BY);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupBy(@Nonnull final String[] groupBy) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");

        return new GroupFlux(this, Arrays.asList(groupBy), GroupFlux.GroupType.GROUP_BY);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @param keep    Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupBy(@Nonnull final String[] groupBy, @Nonnull final String[] keep) {
        Objects.requireNonNull(groupBy, "GroupBy Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this, Arrays.asList(groupBy), Arrays.asList(keep), GroupFlux.GroupType.GROUP_BY);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupByParameterName The parameter name for the group by these specific tag names.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupBy(@Nonnull final String groupByParameterName) {
        Preconditions.checkNonEmptyString(groupByParameterName, "GroupBy parameter name");

        return new GroupFlux(this, groupByParameterName, GroupFlux.GroupType.GROUP_BY);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupByParameterName The parameter name for the group by these specific tag names.
     * @param keepParameterName    The parameter name for the Keep specific tag keys that
     *                             were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupBy(@Nonnull final String groupByParameterName, @Nonnull final String keepParameterName) {
        Preconditions.checkNonEmptyString(groupByParameterName, "GroupBy parameter name");
        Preconditions.checkNonEmptyString(keepParameterName, "Keep parameter name");

        return new GroupFlux(this, groupByParameterName, keepParameterName, GroupFlux.GroupType.GROUP_BY);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupExcept(@Nonnull final Collection<String> except) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");

        return new GroupFlux(this, except, GroupFlux.GroupType.EXCEPT);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @param keep   Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupExcept(@Nonnull final Collection<String> except, @Nonnull final Collection<String> keep) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this, except, keep, GroupFlux.GroupType.EXCEPT);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupExcept(@Nonnull final String[] except) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");

        return new GroupFlux(this, Arrays.asList(except), GroupFlux.GroupType.EXCEPT);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @param keep   Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupExcept(@Nonnull final String[] except, @Nonnull final String[] keep) {
        Objects.requireNonNull(except, "GroupBy Except Columns are required");
        Objects.requireNonNull(keep, "Keep Columns are required");

        return new GroupFlux(this, Arrays.asList(except), Arrays.asList(keep), GroupFlux.GroupType.EXCEPT);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param exceptParameterName The parameter name for the Group by all but these tag keys Cannot be used.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupExcept(@Nonnull final String exceptParameterName) {
        Preconditions.checkNonEmptyString(exceptParameterName, "GroupBy Except parameter name");

        return new GroupFlux(this, exceptParameterName, GroupFlux.GroupType.EXCEPT);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param exceptParameterName The parameter name for the Group by all but these tag keys Cannot be used.
     * @param keepParameterName   The parameter name for the Keep specific tag keys that
     *                            were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public Flux groupExcept(@Nonnull final String exceptParameterName, @Nonnull final String keepParameterName) {
        Preconditions.checkNonEmptyString(exceptParameterName, "GroupBy Except parameter name");
        Preconditions.checkNonEmptyString(keepParameterName, "Keep parameter name");

        return new GroupFlux(this, exceptParameterName, keepParameterName, GroupFlux.GroupType.EXCEPT);
    }

    /**
     * Returns the last result of the query.
     *
     * @return {@link LastFlux}
     */
    @Nonnull
    public Flux last() {
        return new LastFlux(this);
    }

    /**
     * Returns the last result of the query.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link LastFlux}
     */
    @Nonnull
    public Flux last(final boolean useStartTime) {
        return new LastFlux(this, useStartTime);
    }

    /**
     * Returns the last result of the query.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link LastFlux}
     */
    @Nonnull
    public Flux last(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new LastFlux(this, useStartTimeParameterName);
    }

    /**
     * Restricts the number of rows returned in the results.
     *
     * @param numberOfResults The number of results
     * @return {@link LimitFlux}
     */
    @Nonnull
    public Flux limit(final int numberOfResults) {

        Preconditions.checkPositiveNumber(numberOfResults, "Number of results");

        return new LimitFlux(this, numberOfResults);
    }

    /**
     * Restricts the number of rows returned in the results.
     *
     * @param numberOfResultsParameterName The parameter name for the number of results
     * @return {@link LimitFlux}
     */
    @Nonnull
    public Flux limit(@Nonnull final String numberOfResultsParameterName) {

        Preconditions.checkNonEmptyString(numberOfResultsParameterName, "NumberOfResults");

        return new LimitFlux(this, numberOfResultsParameterName);
    }

    /**
     * Returns the max value within the results.
     *
     * @return {@link MaxFlux}
     */
    @Nonnull
    public Flux max() {
        return new MaxFlux(this);
    }

    /**
     * Returns the max value within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MaxFlux}
     */
    @Nonnull
    public Flux max(final boolean useStartTime) {
        return new MaxFlux(this, useStartTime);
    }

    /**
     * Returns the max value within the results.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link MaxFlux}
     */
    @Nonnull
    public Flux max(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new MaxFlux(this, useStartTimeParameterName);
    }

    /**
     * Returns the mean of the values within the results.
     *
     * @return {@link MeanFlux}
     */
    @Nonnull
    public Flux mean() {
        return new MeanFlux(this);
    }

    /**
     * Returns the mean of the values within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MeanFlux}
     */
    @Nonnull
    public Flux mean(final boolean useStartTime) {
        return new MeanFlux(this, useStartTime);
    }

    /**
     * Returns the mean of the values within the results.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link MeanFlux}
     */
    @Nonnull
    public Flux mean(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new MeanFlux(this, useStartTimeParameterName);
    }

    /**
     * Returns the min value within the results.
     *
     * @return {@link MinFlux}
     */
    @Nonnull
    public Flux min() {
        return new MinFlux(this);
    }

    /**
     * Returns the min value within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MinFlux}
     */
    @Nonnull
    public Flux min(final boolean useStartTime) {
        return new MinFlux(this, useStartTime);
    }

    /**
     * Returns the min value within the results.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link MinFlux}
     */
    @Nonnull
    public Flux min(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new MinFlux(this, useStartTimeParameterName);
    }

    /**
     * Skew of the results.
     *
     * @return {@link SkewFlux}
     */
    @Nonnull
    public Flux skew() {
        return new SkewFlux(this);
    }

    /**
     * Skew of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SkewFlux}
     */
    @Nonnull
    public Flux skew(final boolean useStartTime) {
        return new SkewFlux(this, useStartTime);
    }

    /**
     * Skew of the results.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link SkewFlux}
     */
    @Nonnull
    public Flux skew(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new SkewFlux(this, useStartTimeParameterName);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param desc use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public Flux sort(final boolean desc) {
        return new SortFlux(this, desc);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @return {@link SortFlux}
     */
    @Nonnull
    public Flux sort(@Nonnull final String[] columns) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this, Arrays.asList(columns));
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @return {@link SortFlux}
     */
    @Nonnull
    public Flux sort(@Nonnull final Collection<String> columns) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this, columns);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @param desc    use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public Flux sort(@Nonnull final String[] columns, final boolean desc) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this, Arrays.asList(columns), desc);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @param desc    use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public Flux sort(@Nonnull final Collection<String> columns, final boolean desc) {
        Objects.requireNonNull(columns, "Columns are required");

        return new SortFlux(this, columns, desc);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columnsParameterName The parameter name for the columns used to sort
     * @param descParameterName    The parameter name for the use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public Flux sort(@Nonnull final String columnsParameterName, @Nonnull final String descParameterName) {

        Preconditions.checkNonEmptyString(columnsParameterName, "Columns");
        Preconditions.checkNonEmptyString(descParameterName, "desc");

        return new SortFlux(this, descParameterName, columnsParameterName);
    }

    /**
     * Difference between min and max values.
     *
     * @return {@link SpreadFlux}
     */
    @Nonnull
    public Flux spread() {
        return new SpreadFlux(this);
    }

    /**
     * Difference between min and max values.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SpreadFlux}
     */
    @Nonnull
    public Flux spread(final boolean useStartTime) {
        return new SpreadFlux(this, useStartTime);
    }

    /**
     * Difference between min and max values.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link SpreadFlux}
     */
    @Nonnull
    public Flux spread(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new SpreadFlux(this, useStartTimeParameterName);
    }

    /**
     * Standard Deviation of the results.
     *
     * @return {@link StddevFlux}
     */
    @Nonnull
    public Flux stddev() {
        return new StddevFlux(this);
    }

    /**
     * Standard Deviation of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link StddevFlux}
     */
    @Nonnull
    public Flux stddev(final boolean useStartTime) {
        return new StddevFlux(this, useStartTime);
    }

    /**
     * Standard Deviation of the results.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link StddevFlux}
     */
    @Nonnull
    public Flux stddev(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new StddevFlux(this, useStartTimeParameterName);
    }

    /**
     * Sum of the results.
     *
     * @return {@link SumFlux}
     */
    @Nonnull
    public Flux sum() {
        return new SumFlux(this);
    }

    /**
     * Sum of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SumFlux}
     */
    @Nonnull
    public Flux sum(final boolean useStartTime) {
        return new SumFlux(this, useStartTime);
    }

    /**
     * Sum of the results.
     *
     * @param useStartTimeParameterName The parameter name for the use the start time as the timestamp of
     *                                  the resulting aggregate
     * @return {@link SumFlux}
     */
    @Nonnull
    public Flux sum(@Nonnull final String useStartTimeParameterName) {

        Preconditions.checkNonEmptyString(useStartTimeParameterName, "UseStartTime");

        return new SumFlux(this, useStartTimeParameterName);
    }

    /**
     * Convert a value to a bool.
     *
     * @return {@link ToBoolFlux}
     */
    @Nonnull
    public Flux toBool() {
        return new ToBoolFlux(this);
    }

    /**
     * Convert a value to a int.
     *
     * @return {@link ToIntFlux}
     */
    @Nonnull
    public Flux toInt() {
        return new ToIntFlux(this);
    }

    /**
     * Convert a value to a float.
     *
     * @return {@link ToFloatFlux}
     */
    @Nonnull
    public Flux toFloat() {
        return new ToFloatFlux(this);
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
