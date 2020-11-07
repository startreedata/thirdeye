package org.apache.pinot.thirdeye.datalayer.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConfiguration {

  private String user;
  private String password;
  private String url;
  private String driver;
  private Map<String, String> properties = Maps.newLinkedHashMap();

  public String getUser() {
    return user;
  }

  public DatabaseConfiguration setUser(final String user) {
    this.user = user;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public DatabaseConfiguration setPassword(final String password) {
    this.password = password;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public DatabaseConfiguration setUrl(final String url) {
    this.url = url;
    return this;
  }

  public String getDriver() {
    return driver;
  }

  public DatabaseConfiguration setDriver(final String driver) {
    this.driver = driver;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public DatabaseConfiguration setProperties(
      final Map<String, String> properties) {
    this.properties = properties;
    return this;
  }
}
