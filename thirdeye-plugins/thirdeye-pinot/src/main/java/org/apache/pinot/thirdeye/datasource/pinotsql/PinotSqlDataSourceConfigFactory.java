package org.apache.pinot.thirdeye.datasource.pinotsql;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlDataSourceConfigFactory {

  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";
  private static final Logger LOG = LoggerFactory.getLogger(PinotSqlDataSourceConfigFactory.class);

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns PinotSqlThirdEyeDataSourceConfig from the given property map.
   *
   * @param properties the properties to setup a PinotThirdEyeDataSourceConfig.
   * @return a PinotSqlThirdEyeDataSourceConfig.
   * @throws IllegalArgumentException is thrown if the property map does not contain all
   *     necessary fields, i.e.,
   *     controller host and port, cluster name, and the URL to zoo keeper.
   */
  public static PinotSqlThirdEyeDataSourceConfig createFromProperties(
      Map<String, Object> properties) {

    ImmutableMap<String, Object> processedProperties = processPropertyMap(properties);
    checkNotNull(processedProperties, "Invalid properties for data source. properties=%s", properties);

    String controllerHost = MapUtils.getString(processedProperties,
        PinotSqlDataSourceProperties.CONTROLLER_HOST.getValue());
    int controllerPort = MapUtils.getInteger(processedProperties,
        PinotSqlDataSourceProperties.CONTROLLER_PORT.getValue());
    String controllerConnectionScheme = MapUtils.getString(processedProperties,
        PinotSqlDataSourceProperties.CONTROLLER_CONNECTION_SCHEME.getValue());
    Map<String, String> headers = (Map<String, String>) MapUtils.getMap(processedProperties,
      PinotSqlDataSourceProperties.CONTROLLER_HEADERS.getValue());

    Builder builder =
        builder().setControllerHost(controllerHost)
            .setControllerPort(controllerPort);
    if (StringUtils.isNotBlank(controllerConnectionScheme)) {
      builder.setControllerConnectionScheme(controllerConnectionScheme);
    }
    Optional.ofNullable(headers).ifPresent(h -> builder.setHeaders(h));
    return builder.build();
  }

  /**
   * Process the input properties and Checks if the given property map could be used to construct a
   * PinotThirdEyeDataSourceConfig. The essential fields are controller host and port, cluster name,
   * and the URL to zoo
   * keeper. This method prints out all missing essential fields before returning a null processed
   * map.
   *
   * @param properties the input properties to be checked.
   * @return a processed property map; null if the given property map cannot be validated successfully.
   */
  static ImmutableMap<String, Object> processPropertyMap(Map<String, Object> properties) {
    if (MapUtils.isEmpty(properties)) {
      LOG.error("PinotSqlThirdEyeDataSource is missing properties {}", properties);
      return null;
    }

    final List<PinotSqlDataSourceProperties> requiredProperties = Arrays
        .asList(PinotSqlDataSourceProperties.CONTROLLER_HOST,
            PinotSqlDataSourceProperties.CONTROLLER_PORT);

    final List<PinotSqlDataSourceProperties> optionalProperties = Collections.singletonList(
        PinotSqlDataSourceProperties.CONTROLLER_CONNECTION_SCHEME);

    // Validates required properties
    final String className = PinotSqlControllerResponseCacheLoader.class.getSimpleName();
    boolean valid = true;
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    for (PinotSqlDataSourceProperties requiredProperty : requiredProperties) {
      String propertyString = nullToEmpty(properties.get(requiredProperty.getValue())).trim();
      if (Strings.isNullOrEmpty(propertyString)) {
        valid = false;
        LOG.error("{} is missing required property {}", className, requiredProperty);
      } else {
        builder.put(requiredProperty.getValue(), propertyString);
      }
    }

    if (valid) {
      // Copies optional properties
      for (PinotSqlDataSourceProperties optionalProperty : optionalProperties) {
        String propertyString = nullToEmpty(properties.get(optionalProperty.getValue())).trim();
        if (!Strings.isNullOrEmpty(propertyString)) {
          builder.put(optionalProperty.getValue(), propertyString);
        }
      }
      // Handling separately as headers value is a map.
      // TODO make the above iterator logic generic
      String headers = PinotSqlDataSourceProperties.CONTROLLER_HEADERS.getValue();
      if(properties.containsKey(headers)){
        builder.put(headers, properties.get(headers));
      }
      return builder.build();
    } else {
      return null;
    }
  }

  private static String nullToEmpty(Object value) {
    if (value == null) {
      return "";
    }
    return value.toString();
  }

  public static class Builder {

    private String controllerHost;
    private int controllerPort = -1;
    private String controllerConnectionScheme = HTTP_SCHEME;
    private Map<String, String> headers = new HashMap<>();

    public Builder setControllerHost(String controllerHost) {
      this.controllerHost = controllerHost;
      return this;
    }

    public Builder setControllerPort(int controllerPort) {
      this.controllerPort = controllerPort;
      return this;
    }

    public Builder setControllerConnectionScheme(String controllerConnectionScheme) {
      this.controllerConnectionScheme = controllerConnectionScheme;
      return this;
    }

    public Builder setHeaders(final Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    public PinotSqlThirdEyeDataSourceConfig build() {
      final String className = PinotSqlThirdEyeDataSourceConfig.class.getSimpleName();
      checkNotNull(controllerHost, "{} is missing 'Controller Host' property", className);
      checkArgument(controllerPort >= 0, "{} is missing 'Controller Port' property",
          className);
      checkArgument(
          controllerConnectionScheme.equals(HTTP_SCHEME) || controllerConnectionScheme
              .equals(HTTPS_SCHEME),
          "{} accepts only 'http' or 'https' connection schemes", className);

      return new PinotSqlThirdEyeDataSourceConfig()
          .setControllerHost(controllerHost)
          .setControllerPort(controllerPort)
          .setControllerConnectionScheme(controllerConnectionScheme)
          .setHeaders(headers);
    }
  }
}
