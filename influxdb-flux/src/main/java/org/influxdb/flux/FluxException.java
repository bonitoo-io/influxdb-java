package org.influxdb.flux;

import retrofit2.HttpException;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 11:53)
 * @since 3.0.0
 */
public class FluxException extends RuntimeException {

    public FluxException(@Nullable final String message) {
        super(message);
    }

    private FluxException(@Nullable final Throwable cause) {
        super(cause);
    }

    private FluxException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }

    @Nonnull
    public static FluxException fromCause(@Nullable final Throwable cause) {
        if (cause instanceof HttpException) {
            Response<?> response = ((HttpException) cause).response();

            String errorHeader = getErrorMessage(response);

            if (errorHeader != null && !errorHeader.isEmpty()) {
                return new FluxException(errorHeader, cause);
            }
        }

        return new FluxException(cause);
    }

    @Nullable
    public static String getErrorMessage(@Nullable final Response<?> response) {

        if (response == null) {
            return null;
        }

        return response.headers().get("X-Influx-Error");
    }
}
