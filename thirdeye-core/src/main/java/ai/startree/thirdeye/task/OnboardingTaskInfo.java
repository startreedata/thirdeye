/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

public class OnboardingTaskInfo extends DetectionPipelineTaskInfo {

  private long tuningWindowStart;
  private long tuningWindowEnd;

  public long getTuningWindowStart() {
    return tuningWindowStart;
  }

  public OnboardingTaskInfo setTuningWindowStart(final long tuningWindowStart) {
    this.tuningWindowStart = tuningWindowStart;
    return this;
  }

  public long getTuningWindowEnd() {
    return tuningWindowEnd;
  }

  public OnboardingTaskInfo setTuningWindowEnd(final long tuningWindowEnd) {
    this.tuningWindowEnd = tuningWindowEnd;
    return this;
  }
}
