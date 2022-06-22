/*
 * Copyright 2022 StarTree Inc
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
