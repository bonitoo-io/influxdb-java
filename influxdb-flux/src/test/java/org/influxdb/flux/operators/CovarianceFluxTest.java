package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jakub Bednar (bednar@github) (17/07/2018 13:51)
 */
@RunWith(JUnitPlatform.class)
class CovarianceFluxTest {

    @Test
    void covarianceByColumnsArray() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"});

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_value\", \"_oldValue\"])";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByColumnsCollection() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns);

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_time\", \"_value\"])";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByColumnsArrayValueDst() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"}, "_covValue");

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_value\", \"_oldValue\"], valueDst: \"_covValue\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByColumnsCollectionValueDst() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns, "_covValue");

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_time\", \"_value\"], valueDst: \"_covValue\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByColumnsArrayPearsonrValueDst() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"}, true, "_covValue");

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_value\", \"_oldValue\"], pearsonr: true, valueDst: \"_covValue\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByColumnsCollectionPearsonr() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns, false);

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_time\", \"_value\"], pearsonr: false)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByColumnsArrayPearsonr() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"}, true);

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_value\", \"_oldValue\"], pearsonr: true)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByColumnsCollectionPearsonrValueDst() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns, false, "_covValue");

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"_time\", \"_value\"], pearsonr: false, valueDst: \"_covValue\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void covarianceByProperties() {

        Flux flux = Flux
                .from("telegraf")
                .covariance()
                    .withColumns(new String[]{"columnA", "columnB"})
                    .withPearsonr(true)
                    .withValueDst("_newColumn");

        String expected = "from(db:\"telegraf\") |> covariance(columns: [\"columnA\", \"columnB\"], pearsonr: true, valueDst: \"_newColumn\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void expectedExactlyTwoColumnsArray() {

        CovarianceFlux flux = Flux.from("telegraf").covariance();

        Assertions.assertThatThrownBy(() -> flux.withColumns(new String[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        Assertions.assertThatThrownBy(() -> flux.withColumns(new String[]{"val1"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        Assertions.assertThatThrownBy(() -> flux.withColumns(new String[]{"val1", "val2", "val3"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");
    }
    @Test
    void expectedExactlyTwoColumnsCollection() {

        CovarianceFlux flux = Flux.from("telegraf").covariance();

        Collection<String> columns = new ArrayList<>();

        Assertions.assertThatThrownBy(() -> flux.withColumns(columns))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        columns.add("val1");
        Assertions.assertThatThrownBy(() -> flux.withColumns(columns))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        columns.add("val2");
        columns.add("val3");
        Assertions.assertThatThrownBy(() -> flux.withColumns(columns))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");
    }
}