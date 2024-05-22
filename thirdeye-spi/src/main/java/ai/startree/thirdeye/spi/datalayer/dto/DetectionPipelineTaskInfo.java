/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.spi.datalayer.dto;

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

  @Override
  public long getRefId() {
    return getConfigId();
  }
}
