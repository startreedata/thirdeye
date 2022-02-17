/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

import ai.startree.thirdeye.spi.task.TaskInfo;

public class DetectionPipelineTaskInfo implements TaskInfo {

  long configId;
  long start;
  long end;

  public DetectionPipelineTaskInfo(long configId, long start, long end) {
    this.configId = configId;
    this.start = start;
    this.end = end;
  }

  public DetectionPipelineTaskInfo() {
    // dummy constructor for deserialization
  }

  public long getConfigId() {
    return configId;
  }

  public DetectionPipelineTaskInfo setConfigId(final long configId) {
    this.configId = configId;
    return this;
  }

  public long getStart() {
    return start;
  }

  public DetectionPipelineTaskInfo setStart(final long start) {
    this.start = start;
    return this;
  }

  public long getEnd() {
    return end;
  }

  public DetectionPipelineTaskInfo setEnd(final long end) {
    this.end = end;
    return this;
  }
}
