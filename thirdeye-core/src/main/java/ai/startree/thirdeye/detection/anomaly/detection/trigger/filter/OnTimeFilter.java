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
package ai.startree.thirdeye.detection.anomaly.detection.trigger.filter;

import ai.startree.thirdeye.detection.anomaly.detection.trigger.DataAvailabilityEvent;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.utils.DatasetTriggerInfoRepo;

/**
 * This class is to filter out out-of-order events.
 */
public class OnTimeFilter implements DataAvailabilityEventFilter {

  private final DatasetTriggerInfoRepo datasetTriggerInfoRepo;

  public OnTimeFilter(final DatasetTriggerInfoRepo datasetTriggerInfoRepo) {
    this.datasetTriggerInfoRepo = datasetTriggerInfoRepo;
  }

  @Override
  public boolean isPassed(DataAvailabilityEvent e) {
    return (e.getHighWatermark() > datasetTriggerInfoRepo
        .getLastUpdateTimestamp(e.getDatasetName()));
  }
}
