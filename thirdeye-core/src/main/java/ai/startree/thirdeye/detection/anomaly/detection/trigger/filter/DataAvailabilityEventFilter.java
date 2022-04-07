/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.detection.trigger.filter;

import ai.startree.thirdeye.detection.anomaly.detection.trigger.DataAvailabilityEvent;

public interface DataAvailabilityEventFilter {

  boolean isPassed(DataAvailabilityEvent e);
}
