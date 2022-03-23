/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import java.lang.reflect.Constructor;

/**
 * Helper methods to load cache config
 */
public class CacheConfigLoader {

  public static CacheDAO loadCacheDAO(CacheConfig config) throws Exception {
    String className = config.getCentralizedCacheConfig().getDataSourceConfig().getClassName();
    Constructor<?> constructor = Class.forName(className).getConstructor();
    return (CacheDAO) constructor.newInstance();
  }
}
