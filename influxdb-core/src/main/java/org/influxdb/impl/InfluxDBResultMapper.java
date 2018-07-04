/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 azeti Networks AG (<info@azeti.net>)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.influxdb.impl;

import org.influxdb.InfluxDBMapperException;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.QueryResult;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Main class responsible for mapping a QueryResult to a POJO.
 *
 * @author fmachado
 */
public class InfluxDBResultMapper extends AbstractInfluxDBMapper {

  /**
   * <p>
   * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
   * data structure and creating the respective object instances based on the Class passed as
   * parameter.
   * </p>
   *
   * @param queryResult the InfluxDB result object
   * @param clazz the Class that will be used to hold your measurement data
   * @param <T> the target type
   *
   * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
   * same order as received from InfluxDB.
   *
   * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
   * <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
   * possible to define the values of your POJO (e.g. due to an unsupported field type).
   */
  public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz) throws InfluxDBMapperException {
    return toPOJO(queryResult, clazz, TimeUnit.MILLISECONDS);
  }

  /**
   * <p>
   * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
   * data structure and creating the respective object instances based on the Class passed as
   * parameter.
   * </p>
   *
   * @param queryResult the InfluxDB result object
   * @param clazz the Class that will be used to hold your measurement data
   * @param precision the time unit of the results.
   * @param <T> the target type
   *
   * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
   * same order as received from InfluxDB.
   *
   * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
   * <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
   * possible to define the values of your POJO (e.g. due to an unsupported field type).
   */
  public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz, @Nonnull final TimeUnit precision)
          throws InfluxDBMapperException {

    Objects.requireNonNull(precision, "TimeUnit precision is required");

    throwExceptionIfMissingAnnotation(clazz);
    String measurementName = getMeasurementName(clazz);
    return this.toPOJO(queryResult, clazz, measurementName, precision);
  }

  /**
   * <p>
   * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
   * data structure and creating the respective object instances based on the Class passed as
   * parameter.
   * </p>
   *
   * @param queryResult the InfluxDB result object
   * @param clazz the Class that will be used to hold your measurement data
   * @param <T> the target type
   * @param measurementName name of the Measurement
   *
   * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
   * same order as received from InfluxDB.
   *
   * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
   * <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
   * possible to define the values of your POJO (e.g. due to an unsupported field type).
   */
  public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz, final String measurementName)
      throws InfluxDBMapperException {
    return toPOJO(queryResult, clazz, measurementName, TimeUnit.MILLISECONDS);
  }

  /**
   * <p>
   * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
   * data structure and creating the respective object instances based on the Class passed as
   * parameter.
   * </p>
   *
   * @param queryResult the InfluxDB result object
   * @param clazz the Class that will be used to hold your measurement data
   * @param <T> the target type
   * @param measurementName name of the Measurement
   * @param precision the time unit of the results.
   *
   * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
   * same order as received from InfluxDB.
   *
   * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
   * <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
   * possible to define the values of your POJO (e.g. due to an unsupported field type).
   */
  public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz, final String measurementName,
                            @Nonnull final TimeUnit precision)
      throws InfluxDBMapperException {

    Objects.requireNonNull(measurementName, "measurementName");
    Objects.requireNonNull(queryResult, "queryResult");
    Objects.requireNonNull(clazz, "clazz");

    throwExceptionIfResultWithError(queryResult);
    cacheMeasurementClass(clazz);

    List<T> result = new LinkedList<T>();

    queryResult.getResults().stream()
      .filter(internalResult -> Objects.nonNull(internalResult) && Objects.nonNull(internalResult.getSeries()))
      .forEach(internalResult -> {
        internalResult.getSeries().stream()
          .filter(series -> series.getName().equals(measurementName))
          .forEachOrdered(series -> {
            parseSeriesAs(series, clazz, result, precision);
          });
        });

    return result;
  }

  void throwExceptionIfMissingAnnotation(final Class<?> clazz) {
    if (!clazz.isAnnotationPresent(Measurement.class)) {
      throw new IllegalArgumentException(
        "Class " + clazz.getName() + " is not annotated with @" + Measurement.class.getSimpleName());
    }
  }

  void throwExceptionIfResultWithError(final QueryResult queryResult) {
    if (queryResult.getError() != null) {
      throw new InfluxDBMapperException("InfluxDB returned an error: " + queryResult.getError());
    }

    queryResult.getResults().forEach(seriesResult -> {
      if (seriesResult.getError() != null) {
        throw new InfluxDBMapperException("InfluxDB returned an error with Series: " + seriesResult.getError());
      }
    });
  }

  <T> List<T> parseSeriesAs(final QueryResult.Series series, final Class<T> clazz, final List<T> result) {
    return parseSeriesAs(series, clazz, result, TimeUnit.MILLISECONDS);
  }

  <T> List<T> parseSeriesAs(final QueryResult.Series series, final Class<T> clazz, final List<T> result,
                            @Nonnull final TimeUnit precision) {

    Objects.requireNonNull(precision, "TimeUnit precision is required");

    int columnSize = series.getColumns().size();
    ConcurrentMap<String, Field> colNameAndFieldMap = CLASS_FIELD_CACHE.get(clazz.getName());
    try {
      T object = null;
      for (List<Object> row : series.getValues()) {
        for (int i = 0; i < columnSize; i++) {
          Field correspondingField = colNameAndFieldMap.get(series.getColumns().get(i)/*InfluxDB columnName*/);
          if (correspondingField != null) {
            if (object == null) {
              object = clazz.newInstance();
            }
            setFieldValue(object, correspondingField, row.get(i), precision);
          }
        }
        // When the "GROUP BY" clause is used, "tags" are returned as Map<String,String> and
        // accordingly with InfluxDB documentation
        // https://docs.influxdata.com/influxdb/v1.2/concepts/glossary/#tag-value
        // "tag" values are always String.
        if (series.getTags() != null && !series.getTags().isEmpty()) {
          for (Entry<String, String> entry : series.getTags().entrySet()) {
            Field correspondingField = colNameAndFieldMap.get(entry.getKey()/*InfluxDB columnName*/);
            if (correspondingField != null) {
              // I don't think it is possible to reach here without a valid "object"
              setFieldValue(object, correspondingField, entry.getValue(), precision);
            }
          }
        }
        if (object != null) {
          result.add(object);
          object = null;
        }
      }
    } catch (InstantiationException | IllegalAccessException e) {
      throw new InfluxDBMapperException(e);
    }
    return result;
  }

}
