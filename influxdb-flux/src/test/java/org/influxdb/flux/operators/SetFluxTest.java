package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2018 09:32)
 */
@RunWith(JUnitPlatform.class)
class SetFluxTest {

    @Test
    void set() {

        Flux flux = Flux
                .from("telegraf")
                .set("location", "Carolina");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> set(key: \"location\", value: \"Carolina\")");
    }

    @Test
    void setByWith() {

        Flux flux = Flux
                .from("telegraf")
                .set()
                .withKeyValue("type", "telegraphs");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> set(key: \"type\", value: \"telegraphs\")");
    }
}