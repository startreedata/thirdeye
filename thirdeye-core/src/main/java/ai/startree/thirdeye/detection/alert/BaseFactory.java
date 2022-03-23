/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert;

/**
 * This BaseFactory is responsible to generate an instance from a given class path.
 *
 * @param <T> the class of return instance
 */
public class BaseFactory<T> {

  public <T> T getInstance(String classPath)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    T typeInstance = (T) Class.forName(classPath).newInstance();
    return typeInstance;
  }
}
