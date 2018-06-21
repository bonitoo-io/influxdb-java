package org.influxdb.impl;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;
import java.util.Random;

/**
 * @author Jakub Bednar (bednar@github) (21/06/2018 08:18)
 */
public abstract class AbstractMapperTest {

    @Measurement(name = "CustomMeasurement")
    static class MyCustomMeasurement {

        @Column(name = "time")
        Instant time;

        @Column(name = "uuid")
        String uuid;

        @Column(name = "doubleObject")
        Double doubleObject;

        @Column(name = "longObject")
        Long longObject;

        @Column(name = "integerObject")
        Integer integerObject;

        @Column(name = "doublePrimitive")
        double doublePrimitive;

        @Column(name = "longPrimitive")
        long longPrimitive;

        @Column(name = "integerPrimitive")
        int integerPrimitive;

        @Column(name = "booleanObject")
        Boolean booleanObject;

        @Column(name = "booleanPrimitive")
        boolean booleanPrimitive;

        @SuppressWarnings("unused")
        String nonColumn1;

        @SuppressWarnings("unused")
        Random rnd;

        @Override
        public String toString() {
            return "MyCustomMeasurement [time=" + time + ", uuid=" + uuid + ", doubleObject=" + doubleObject + ", longObject=" + longObject
                    + ", integerObject=" + integerObject + ", doublePrimitive=" + doublePrimitive + ", longPrimitive=" + longPrimitive
                    + ", integerPrimitive=" + integerPrimitive + ", booleanObject=" + booleanObject + ", booleanPrimitive=" + booleanPrimitive + "]";
        }
    }
}
