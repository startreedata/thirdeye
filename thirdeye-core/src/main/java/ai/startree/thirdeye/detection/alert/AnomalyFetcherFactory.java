/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert;

import ai.startree.thirdeye.detection.alert.fetcher.AnomalyFetcher;

public class AnomalyFetcherFactory extends BaseFactory<AnomalyFetcher> {

  public static final String PACKAGE_PATH = "ai.startree.thirdeye.alert.fetcher";

  public static AnomalyFetcher fromClassName(String className)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    String classPath = PACKAGE_PATH + "." + className;
    return (new BaseFactory<AnomalyFetcher>()).getInstance(classPath);
  }
}
