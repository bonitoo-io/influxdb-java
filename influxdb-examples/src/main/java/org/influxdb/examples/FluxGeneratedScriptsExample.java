package org.influxdb.examples;

import org.influxdb.flux.Flux;
import org.influxdb.flux.operators.restriction.Restrictions;

import java.time.temporal.ChronoUnit;

/**
 * @author Jakub Bednar (bednar@github) (19/07/2018 09:55)
 */
public class FluxGeneratedScriptsExample {

    public static void main(String[] args) {

        Restrictions restrictions = Restrictions.and(
                Restrictions.measurement().equal("cpu"),
                Restrictions.field().equal("usage_user"));

        Flux flux1 = Flux.from("telegraf")
                .range()
                    .withStart(-1L, ChronoUnit.HOURS)
                .filter(restrictions)
                .window()
                .   withEvery(10L, ChronoUnit.SECONDS)
                .group()
                    .withBy("region");


        System.out.println("--------- Generated Flux script with range, filter, window, group operator ---------");
        System.out.println();
        System.out.println(flux1.print());
        System.out.println();

        Flux flux2 = Flux.from("telegraf")
                .expression("map(fn: (r) => r._value * r._value)")
                .expression("sum()");

        System.out.println("--------- Generated Flux script with custom expression ---------");
        System.out.println();
        System.out.println(flux2.print());
        System.out.println();
    }
}