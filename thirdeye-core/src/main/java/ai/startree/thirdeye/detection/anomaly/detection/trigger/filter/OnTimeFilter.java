/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
