package org.influxdb.flux.mapper;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.options.FluxCsvParserOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author Jakub Bednar (bednar@github) (16/07/2018 12:26)
 */
@RunWith(JUnitPlatform.class)
class FluxCsvParserTest {

    private FluxCsvParser parser;

    @BeforeEach
    void setUp()
    {
        parser = new FluxCsvParser();
    }

    @Test
    void responseWithMultipleValues() throws IOException {

        // curl -i -XPOST --data-urlencode 'q=from(db: "ubuntu_test") |> last()
        // |> map(fn: (r) => ({value1: r._value, _value2:r._value * r._value, value_str: "test"}))'
        // --data-urlencode "orgName=0" http://localhost:8093/v1/query

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long,long,string\n"
                + "#group,false,false,true,true,true,true,true,true,false,false,false\n"
                + "#default,_result,,,,,,,,,,\n"
                + ",result,table,_start,_stop,_field,_measurement,host,region,_value2,value1,value_str\n"
                + ",,0,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,free,mem,A,west,121,11,test\n"
                + ",,1,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,free,mem,B,west,484,22,test\n"
                + ",,2,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,usage_system,cpu,A,west,1444,38,test\n"
                + ",,3,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,user_usage,cpu,A,west,2401,49,test";

        FluxCsvParserOptions settings = FluxCsvParserOptions
                .builder()
                .valueDestinations("value1", "_value2", "value_str")
                .build();
        
        FluxResult fluxResult = parser.parseFluxResponse(new StringReader(data), settings);

        Assertions.assertThat(fluxResult.getTables()).hasSize(4);

        // Record 1
        Assertions.assertThat(fluxResult.getTables().get(0).getRecords()).hasSize(1);
        Assertions.assertThat(fluxResult.getTables().get(0).getRecords().get(0).getTags()).hasSize(2);
        Assertions.assertThat(fluxResult.getTables().get(0).getRecords().get(0).getTags())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxResult.getTables().get(0).getRecords().get(0).getValues()).hasSize(3);
        Assertions.assertThat(fluxResult.getTables().get(0).getRecords().get(0).getValue()).isEqualTo(11L);
        Assertions.assertThat(fluxResult.getTables().get(0).getRecords().get(0).getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(11L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(121L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

        // Record 2
        Assertions.assertThat(fluxResult.getTables().get(1).getRecords()).hasSize(1);
        Assertions.assertThat(fluxResult.getTables().get(1).getRecords().get(0).getTags()).hasSize(2);
        Assertions.assertThat(fluxResult.getTables().get(1).getRecords().get(0).getTags())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxResult.getTables().get(1).getRecords().get(0).getValues()).hasSize(3);
        Assertions.assertThat(fluxResult.getTables().get(1).getRecords().get(0).getValue()).isEqualTo(22L);
        Assertions.assertThat(fluxResult.getTables().get(1).getRecords().get(0).getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(22L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(484L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

        // Record 3
        Assertions.assertThat(fluxResult.getTables().get(2).getRecords()).hasSize(1);
        Assertions.assertThat(fluxResult.getTables().get(2).getRecords().get(0).getTags()).hasSize(2);
        Assertions.assertThat(fluxResult.getTables().get(2).getRecords().get(0).getTags())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxResult.getTables().get(2).getRecords().get(0).getValues()).hasSize(3);
        Assertions.assertThat(fluxResult.getTables().get(2).getRecords().get(0).getValue()).isEqualTo(38L);
        Assertions.assertThat(fluxResult.getTables().get(2).getRecords().get(0).getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(38L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(1444L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

        // Record 4
        Assertions.assertThat(fluxResult.getTables().get(3).getRecords()).hasSize(1);
        Assertions.assertThat(fluxResult.getTables().get(3).getRecords().get(0).getTags()).hasSize(2);
        Assertions.assertThat(fluxResult.getTables().get(3).getRecords().get(0).getTags())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxResult.getTables().get(3).getRecords().get(0).getValues()).hasSize(3);
        Assertions.assertThat(fluxResult.getTables().get(3).getRecords().get(0).getValue()).isEqualTo(49L);
        Assertions.assertThat(fluxResult.getTables().get(3).getRecords().get(0).getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(49L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(2401L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));
    }
}
