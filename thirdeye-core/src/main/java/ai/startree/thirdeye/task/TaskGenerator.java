/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

import ai.startree.thirdeye.detection.anomaly.monitor.MonitorConfiguration;
import ai.startree.thirdeye.detection.anomaly.monitor.MonitorConstants.MonitorType;
import ai.startree.thirdeye.detection.anomaly.monitor.MonitorJobContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates tasks for a job depending on the task type
 */
public class TaskGenerator {

  public List<MonitorTaskInfo> createMonitorTasks(MonitorJobContext monitorJobContext) {
    List<MonitorTaskInfo> tasks = new ArrayList<>();
    MonitorConfiguration monitorConfiguration = monitorJobContext.getMonitorConfiguration();

    // Generates the task to updating the status of all jobs and tasks
    MonitorTaskInfo updateTaskInfo = new MonitorTaskInfo();
    updateTaskInfo.setMonitorType(MonitorType.UPDATE);
    updateTaskInfo
        .setCompletedJobRetentionDays(monitorConfiguration.getCompletedJobRetentionDays());
    updateTaskInfo.setDefaultRetentionDays(monitorConfiguration.getDefaultRetentionDays());
    updateTaskInfo
        .setDetectionStatusRetentionDays(monitorConfiguration.getDetectionStatusRetentionDays());
    updateTaskInfo.setRawAnomalyRetentionDays(monitorConfiguration.getRawAnomalyRetentionDays());
    tasks.add(updateTaskInfo);

    // Generates the task to expire (delete) old jobs and tasks in DB
    MonitorTaskInfo expireTaskInfo = new MonitorTaskInfo();
    expireTaskInfo.setMonitorType(MonitorType.EXPIRE);
    expireTaskInfo
        .setCompletedJobRetentionDays(monitorConfiguration.getCompletedJobRetentionDays());
    expireTaskInfo.setDefaultRetentionDays(monitorConfiguration.getDefaultRetentionDays());
    expireTaskInfo
        .setDetectionStatusRetentionDays(monitorConfiguration.getDetectionStatusRetentionDays());
    expireTaskInfo.setRawAnomalyRetentionDays(monitorConfiguration.getRawAnomalyRetentionDays());
    tasks.add(expireTaskInfo);

    return tasks;
  }
}
