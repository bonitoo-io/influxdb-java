package org.influxdb.reactive.events;

import okio.BufferedSource;
import org.influxdb.dto.QueryResult;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when is parsed streamed response to query result.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 11:46)
 * @since 3.0.0
 */
public class QueryParsedResponseEvent extends AbstractInfluxEvent {

    private static final Logger LOG = Logger.getLogger(QueryParsedResponseEvent.class.getName());

    private final BufferedSource bufferedSource;
    private final QueryResult queryResult;

    public QueryParsedResponseEvent(@Nonnull final BufferedSource bufferedSource,
                                    @Nonnull final QueryResult queryResult) {

        Objects.requireNonNull(bufferedSource, "BufferedSource is not null");
        Objects.requireNonNull(queryResult, "QueryResult is not null");

        this.bufferedSource = bufferedSource;
        this.queryResult = queryResult;
    }

    /**
     * @return chunked response
     */
    @Nonnull
    public BufferedSource getBufferedSource() {
        return bufferedSource;
    }

    /**
     * @return parsed response
     */
    @Nonnull
    public QueryResult getQueryResult() {
        return queryResult;
    }

    @Override
    public void logEvent() {

        LOG.log(Level.FINEST, "Chunk response parsed to {0}", queryResult);
    }
}
