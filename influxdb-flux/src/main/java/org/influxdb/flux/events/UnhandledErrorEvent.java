package org.influxdb.flux.events;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when occurs a unhandled exception.
 *
 * @author Jakub Bednar (bednar@github) (30/07/2018 14:57)
 * @since 3.0.0
 */
public class UnhandledErrorEvent extends AbstractFluxEvent {

    private static final Logger LOG = Logger.getLogger(UnhandledErrorEvent.class.getName());

    private final Throwable throwable;

    public UnhandledErrorEvent(@Nonnull final Throwable throwable) {

        Objects.requireNonNull(throwable, "Throwable is required");

        this.throwable = throwable;
    }

    /**
     * @return the exception that was throw
     */
    @Nonnull
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void logEvent() {
        LOG.log(Level.SEVERE, "Unexpected error", throwable);
    }
}
