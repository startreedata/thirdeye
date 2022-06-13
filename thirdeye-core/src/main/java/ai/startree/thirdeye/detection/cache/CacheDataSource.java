/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import java.util.Map;

/**
 * Config for a single centralized cache data source.
 * For example, this class could be for Couchbase, or Redis, or Cassandra, etc.
 */
public class CacheDataSource {

  /**
   * class name, e.g. ai.startree.thirdeye.detection.cache.CouchbaseCacheDAO
   */
  private String className;

  /**
   * settings/config for the specific data source. generic since different
   * data stores may have different authentication methods.
   */
  private Map<String, Object> config;

  // left blank
  public CacheDataSource() {
  }

  public String getClassName() {
    return className;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }
}
