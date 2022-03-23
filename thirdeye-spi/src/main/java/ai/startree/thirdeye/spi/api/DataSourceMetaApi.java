/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the config of a metadata loader used in thirdeye
 * Eg: UMPMetadataLoader
 */
public class DataSourceMetaApi {

  private String classRef;
  private Map<String, Object> properties = new HashMap<>();

  public String getClassRef() {
    return classRef;
  }

  public DataSourceMetaApi setClassRef(final String classRef) {
    this.classRef = classRef;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public DataSourceMetaApi setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
