package org.apache.pinot.thirdeye.datasource.pinot;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotThirdEyeDataSourceConfigFactory {

  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSourceConfig.class);

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns PinotThirdEyeDataSourceConfig from the given property map.
   *
   * @param properties the properties to setup a PinotThirdEyeDataSourceConfig.
   * @return a PinotThirdEyeDataSourceConfig.
   * @throws IllegalArgumentException is thrown if the property map does not contain all
   *     necessary fields, i.e.,
   *     controller host and port, cluster name, and the URL to zoo keeper.
   */
  public static PinotThirdEyeDataSourceConfig createFromProperties(Map<String, Object> properties) {
    String dataSourceName = MapUtils
        .getString(properties, PinotThirdeyeDataSourceProperties.NAME.getValue(),
            PinotThirdEyeDataSource.class.getSimpleName());

    ImmutableMap<String, Object> processedProperties = processPropertyMap(properties);
    checkNotNull(processedProperties, "Invalid properties for data source: %s, properties=%s",
        dataSourceName, properties);

    String controllerHost = MapUtils.getString(processedProperties,
        PinotThirdeyeDataSourceProperties.CONTROLLER_HOST.getValue());
    int controllerPort = MapUtils.getInteger(processedProperties,
        PinotThirdeyeDataSourceProperties.CONTROLLER_PORT.getValue());
    String controllerConnectionScheme = MapUtils.getString(processedProperties,
        PinotThirdeyeDataSourceProperties.CONTROLLER_CONNECTION_SCHEME.getValue());
    String zookeeperUrl = MapUtils
        .getString(processedProperties, PinotThirdeyeDataSourceProperties.ZOOKEEPER_URL.getValue());
    String clusterName = MapUtils
        .getString(processedProperties, PinotThirdeyeDataSourceProperties.CLUSTER_NAME.getValue());

    // brokerUrl, tag, and name are optional
    String brokerUrl = MapUtils
        .getString(processedProperties, PinotThirdeyeDataSourceProperties.BROKER_URL.getValue());
    String tag = MapUtils
        .getString(processedProperties, PinotThirdeyeDataSourceProperties.TAG.getValue());
    String name = MapUtils
        .getString(processedProperties, PinotThirdeyeDataSourceProperties.NAME.getValue());
    Map<String, String> headers = (Map<String, String>) MapUtils
      .getMap(processedProperties, PinotThirdeyeDataSourceProperties.HEADERS.getValue());

    Builder builder =
        builder().setControllerHost(controllerHost)
            .setControllerPort(controllerPort)
            .setZookeeperUrl(zookeeperUrl).setClusterName(clusterName);
    if (StringUtils.isNotBlank(brokerUrl)) {
      builder.setBrokerUrl(brokerUrl);
    }
    if (StringUtils.isNotBlank(tag)) {
      builder.setTag(tag);
    }
    if (StringUtils.isNotBlank(name)) {
      builder.setName(name);
    }
    if (MapUtils.isNotEmpty(headers)) {
      builder.setHeaders(headers);
    }
    if (StringUtils.isNotBlank(controllerConnectionScheme)) {
      builder.setControllerConnectionScheme(controllerConnectionScheme);
    }

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
      LOG.error("PinotThirdEyeDataSource is missing properties {}", properties);
      return null;
    }

    final List<PinotThirdeyeDataSourceProperties> requiredProperties = Arrays
        .asList(PinotThirdeyeDataSourceProperties.CONTROLLER_HOST,
            PinotThirdeyeDataSourceProperties.CONTROLLER_PORT,
            PinotThirdeyeDataSourceProperties.ZOOKEEPER_URL,
            PinotThirdeyeDataSourceProperties.CLUSTER_NAME);
    final List<PinotThirdeyeDataSourceProperties> optionalProperties = Arrays
        .asList(PinotThirdeyeDataSourceProperties.CONTROLLER_CONNECTION_SCHEME,
            PinotThirdeyeDataSourceProperties.BROKER_URL, PinotThirdeyeDataSourceProperties.TAG);

    // Validates required properties
    final String className = PinotControllerResponseCacheLoader.class.getSimpleName();
    boolean valid = true;
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    for (PinotThirdeyeDataSourceProperties requiredProperty : requiredProperties) {
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
      for (PinotThirdeyeDataSourceProperties optionalProperty : optionalProperties) {
        String propertyString = nullToEmpty(properties.get(optionalProperty.getValue())).trim();
        if (!Strings.isNullOrEmpty(propertyString)) {
          builder.put(optionalProperty.getValue(), propertyString);
        }
      }
      Optional.ofNullable(properties.get(PinotThirdeyeDataSourceProperties.HEADERS.getValue()))
        .ifPresent(
          headers -> builder.put(PinotThirdeyeDataSourceProperties.HEADERS.getValue(), headers)
        );

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

    private String zookeeperUrl;
    private String controllerHost;
    private int controllerPort = -1;
    private String controllerConnectionScheme = HTTP_SCHEME; // HTTP_SCHEME or HTTPS_SCHEME
    private String clusterName;
    private String brokerUrl;
    private String tag;
    private String name;
    private Map<String, String> headers;

    public Builder setZookeeperUrl(String zookeeperUrl) {
      this.zookeeperUrl = zookeeperUrl;
      return this;
    }

    public Builder setControllerHost(String controllerHost) {
      this.controllerHost = controllerHost;
      return this;
    }

    public Builder setControllerPort(int controllerPort) {
      this.controllerPort = controllerPort;
      return this;
    }

    public Builder setClusterName(String clusterName) {
      this.clusterName = clusterName;
      return this;
    }

    public Builder setBrokerUrl(String brokerUrl) {
      this.brokerUrl = brokerUrl;
      return this;
    }

    public Builder setTag(String tag) {
      this.tag = tag;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
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

    public PinotThirdEyeDataSourceConfig build() {
      final String className = PinotThirdEyeDataSourceConfig.class.getSimpleName();
      checkNotNull(controllerHost, "{} is missing 'Controller Host' property", className);
      checkArgument(controllerPort >= 0, "{} is missing 'Controller Port' property",
          className);
      checkNotNull(zookeeperUrl, "{} is missing 'Zookeeper URL' property", className);
      checkNotNull(clusterName, "{} is missing 'Cluster Name' property", className);
      checkArgument(
          controllerConnectionScheme.equals(HTTP_SCHEME) || controllerConnectionScheme
              .equals(HTTPS_SCHEME),
          "{} accepts only 'http' or 'https' connection schemes", className);

      return new PinotThirdEyeDataSourceConfig()
          .setControllerHost(controllerHost)
          .setControllerPort(controllerPort)
          .setZookeeperUrl(zookeeperUrl)
          .setClusterName(clusterName)
          .setBrokerUrl(brokerUrl)
          .setTag(tag)
          .setControllerConnectionScheme(controllerConnectionScheme).setName(name)
          .setHeaders(headers);
    }
  }
}
