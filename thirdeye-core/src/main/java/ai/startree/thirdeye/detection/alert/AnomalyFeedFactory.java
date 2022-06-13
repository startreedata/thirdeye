/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert;

import ai.startree.thirdeye.detection.alert.feed.AnomalyFeed;

public class AnomalyFeedFactory extends BaseFactory<AnomalyFeed> {

  public static final String PACKAGE_PATH = "ai.startree.thirdeye.alert.feed";

  public static AnomalyFeed fromClassName(String className)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    String classPath = PACKAGE_PATH + "." + className;
    return (new BaseFactory<AnomalyFeed>()).getInstance(classPath);
  }
}
