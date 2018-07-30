package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.HashMap;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 11:55)
 */
@RunWith(JUnitPlatform.class)
class LimitFluxTest {

    @Test
    void limit() {

        Flux flux = Flux
                .from("telegraf")
                .limit(5);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> limit(n: 5)");
    }

    @Test
    void limitPositive() {
        Assertions.assertThatThrownBy(() -> Flux.from("telegraf").limit(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for Number of results");
    }

    @Test
    void limitByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .limit()
                .withPropertyNamed("n", "limit");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 15);

        Assertions.assertThat(flux.print(new FluxChain().addParameters(parameters)))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> limit(n: 15)");
    }

    @Test
    void limitByParameterMissing() {

        Assertions.assertThatThrownBy(() -> Flux.from("telegraf").limit().withPropertyNamed("limit").print())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The parameter 'limit' is not defined.");
    }
}