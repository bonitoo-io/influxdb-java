package org.influxdb.flux.operators.restriction;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.regex.Pattern;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 13:16)
 */
@RunWith(JUnitPlatform.class)
class RestrictionTest {

    @Test
    void isEqualTo() {

        Assertions.assertThat(Restrictions.measurement().equal("mem"))
                .hasToString("r[\"_measurement\"] == \"mem\"");

        Assertions.assertThat(Restrictions.field()
                .equal(10)).hasToString("r[\"_field\"] == 10");

        Assertions.assertThat(Restrictions.tag("location")
                .equal(Pattern.compile("/var/"))).hasToString("r[\"location\"] == /var/");
    }

    @Test
    void isNotEqualTo() {

        Assertions.assertThat(Restrictions.measurement().notEqual("mem"))
                .hasToString("r[\"_measurement\"] != \"mem\"");
    }

    @Test
    void less() {
        Assertions.assertThat(Restrictions.start().less(12L)).hasToString("r[\"_start\"] < 12");
    }

    @Test
    void greater() {
        Assertions.assertThat(Restrictions.stop().greater(15)).hasToString("r[\"_stop\"] > 15");
    }

    @Test
    void lessOrEqual() {
        Assertions.assertThat(Restrictions.time().lessOrEqual(20)).hasToString("r[\"_time\"] <= 20");
    }

    @Test
    void greaterOrEqual() {
        Assertions.assertThat(Restrictions.value().greaterOrEqual(20D)).hasToString("r[\"_value\"] >= 20.0");
    }

    @Test
    void columnRestrictions() {
        Assertions.assertThat(Restrictions.column("custom_column").equal(20D))
                .hasToString("r[\"custom_column\"] == 20.0");
    }

    @Test
    void custom() {
        Assertions.assertThat(Restrictions.value().custom(15L, "=~")).hasToString("r[\"_value\"] =~ 15");
    }
}