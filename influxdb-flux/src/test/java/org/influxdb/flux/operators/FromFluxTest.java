package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (22/06/2018 12:04)
 */
@RunWith(JUnitPlatform.class)
class FromFluxTest {

    @Test
    void database() {

        Flux flux = Flux.from("telegraf");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\")");
    }

    @Test
    void databaseRequired() {

        Assertions.assertThatThrownBy(() -> FromFlux.from(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for Database name");
    }

    @Test
    void hostCollection() {

        List<String> hosts = new ArrayList<>();
        hosts.add("fluxdHost");
        hosts.add("192.168.1.100");

        Flux flux = Flux.from("telegraf", hosts);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\", hosts:[\"fluxdHost\", \"192.168.1.100\"])");
    }

    @Test
    void hostCollectionEmpty() {

        Flux flux = Flux.from("telegraf", new ArrayList<>());

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\")");
    }

    @Test
    void hostCollectionRequired() {

        Assertions.assertThatThrownBy(() -> FromFlux.from("telegraf", (Collection<String>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Hosts are required");
    }

    @Test
    void hostArray() {

        Flux flux = Flux.from("telegraf", new String[]{"fluxdHost", "192.168.1.100"});

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\", hosts:[\"fluxdHost\", \"192.168.1.100\"])");
    }

    @Test
    void hostArrayEmpty() {

        Flux flux = Flux.from("telegraf", new String[]{});

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\")");
    }

    @Test
    void hostArrayRequired() {

        Assertions.assertThatThrownBy(() -> FromFlux.from("telegraf", (String[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Hosts are required");
    }
}