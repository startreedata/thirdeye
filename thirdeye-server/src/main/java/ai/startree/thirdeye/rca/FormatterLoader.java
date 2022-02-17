/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

public class FormatterLoader {
  public static RootCauseEntityFormatter fromClassName(String className) throws Exception {
    return (RootCauseEntityFormatter)Class.forName(className).getConstructor().newInstance();
  }
}
