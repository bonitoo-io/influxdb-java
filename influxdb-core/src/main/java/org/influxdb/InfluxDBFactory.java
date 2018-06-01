package org.influxdb;

import org.influxdb.impl.InfluxDBImpl;

import okhttp3.OkHttpClient;
import org.influxdb.impl.Preconditions;

import java.util.Objects;


/**
 * A Factory to create a instance of a InfluxDB Database adapter.
 *
 * @author stefan.majer [at] gmail.com
 *
 */
public enum InfluxDBFactory {
  INSTANCE;

  /**
   * Create a connection to a InfluxDB.
   *
   * @param url
   *            the url to connect to.
   * @return a InfluxDB adapter suitable to access a InfluxDB.
   */
  public static InfluxDB connect(final String url) {
    Preconditions.checkNonEmptyString(url, "url");

    InfluxDBOptions options = InfluxDBOptions.builder().url(url).build();
    return new InfluxDBImpl(options);
  }

  /**
   * Create a connection to a InfluxDB.
   *
   * @param url
   *            the url to connect to.
   * @param username
   *            the username which is used to authorize against the influxDB instance.
   * @param password
   *            the password for the username which is used to authorize against the influxDB
   *            instance.
   * @return a InfluxDB adapter suitable to access a InfluxDB.
   */
  public static InfluxDB connect(final String url, final String username, final String password) {
    Preconditions.checkNonEmptyString(url, "url");
    Preconditions.checkNonEmptyString(username, "username");

    InfluxDBOptions options = InfluxDBOptions.builder()
            .url(url)
            .username(username)
            .password(password)
            .build();

    return new InfluxDBImpl(options);
  }

  /**
   * Create a connection to a InfluxDB.
   *
   * @param url
   *            the url to connect to.
   * @param client
   *            the HTTP client to use
   * @return a InfluxDB adapter suitable to access a InfluxDB.
   */
  public static InfluxDB connect(final String url, final OkHttpClient.Builder client) {
    Preconditions.checkNonEmptyString(url, "url");
    Objects.requireNonNull(client, "client");

    InfluxDBOptions options = InfluxDBOptions.builder()
            .url(url)
            .okHttpClient(client)
            .build();

    return new InfluxDBImpl(options);
  }

  /**
   * Create a connection to a InfluxDB.
   *
   * @param url
   *            the url to connect to.
   * @param username
   *            the username which is used to authorize against the influxDB instance.
   * @param password
   *            the password for the username which is used to authorize against the influxDB
   *            instance.
   * @param client
   *            the HTTP client to use
   * @return a InfluxDB adapter suitable to access a InfluxDB.
   */
  public static InfluxDB connect(final String url, final String username, final String password,
      final OkHttpClient.Builder client) {
    Preconditions.checkNonEmptyString(url, "url");
    Preconditions.checkNonEmptyString(username, "username");
    Objects.requireNonNull(client, "client");

    InfluxDBOptions options = InfluxDBOptions.builder()
            .url(url)
            .username(username)
            .password(password)
            .okHttpClient(client)
            .build();

    return new InfluxDBImpl(options);
  }
}
