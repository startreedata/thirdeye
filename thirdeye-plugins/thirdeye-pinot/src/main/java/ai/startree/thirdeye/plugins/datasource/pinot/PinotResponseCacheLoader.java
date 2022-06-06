/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import com.google.common.cache.CacheLoader;
import java.util.Map;
import org.apache.pinot.client.Connection;

public abstract class PinotResponseCacheLoader extends
    CacheLoader<PinotQuery, ThirdEyeResultSetGroup> {

  /**
   * Initializes the cache loader using the given property map.
   *
   * @param properties the property map that provides the information to connect to the data
   *     source.
   * @throws Exception when an error occurs connecting to the Pinot controller.
   */
  public abstract void init(Map<String, Object> properties) throws Exception;

  public abstract Connection getConnection();

  public abstract void close();
}
