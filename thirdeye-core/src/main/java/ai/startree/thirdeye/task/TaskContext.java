/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;

public class TaskContext {

  private ThirdEyeServerConfiguration thirdEyeServerConfiguration;

  public ThirdEyeServerConfiguration getThirdEyeWorkerConfiguration() {
    return thirdEyeServerConfiguration;
  }

  public TaskContext setThirdEyeWorkerConfiguration(
      ThirdEyeServerConfiguration thirdEyeServerConfiguration) {
    this.thirdEyeServerConfiguration = thirdEyeServerConfiguration;
    return this;
  }
}
