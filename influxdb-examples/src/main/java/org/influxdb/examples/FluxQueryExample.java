package org.influxdb.examples;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxReactive;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.mapper.Record;
import org.influxdb.flux.mapper.Table;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.influxdb.flux.options.FluxOptions;
import org.influxdb.impl.FluxReactiveImpl;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (19/07/2018 10:09)
 */
public class FluxQueryExample {

    public static void main(String[] args) {

        Restrictions restrictions = Restrictions.and(
                Restrictions.measurement().equal("cpu"),
                Restrictions.field().equal("usage_user"));

        Flux flux = Flux
                .from("telegraf")
                .range(-5L, ChronoUnit.MINUTES)
                .filter(restrictions)
                .window()
                    .withEvery(10L, ChronoUnit.SECONDS)
                .group()
                    .withBy("host")
                .limit(5);

        System.out.println("--------- Generated Flux script ---------");
        System.out.println();
        System.out.println(flux.print());
        System.out.println();

        System.out.println("--------- Flux script results  ---------");
        System.out.println();

        FluxOptions fluxOptions = FluxOptions.builder()
                .url("http://127.0.0.1:8093")
                .orgID("00")
                .build();

        FluxReactive fluxReactive = new FluxReactiveImpl(fluxOptions);
        List<FluxResult> fluxResults = fluxReactive.flux(flux).toList().blockingGet();
        for (FluxResult fluxResult : fluxResults) {

            for (Table table : fluxResult.getTables())
            {
                for (Record record : table.getRecords())
                {
                    System.out.println(">> " + record);
                }

                System.out.println();
            }
        }
    }
}