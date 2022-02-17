/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.job;

import java.util.List;

/**
 * An interface for ReplayTaskRunner classes of the executors
 */
public interface JobRunner extends Runnable {

  Long createJob();

  List<Long> createTasks();
}
