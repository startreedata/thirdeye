/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.task.runner;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.detection.anomaly.monitor.MonitorConstants.MonitorType;
import org.apache.pinot.thirdeye.detection.anomaly.monitor.MonitorJobRunner;
import org.apache.pinot.thirdeye.spi.Constants.JobStatus;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DetectionStatusManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.JobManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.OnlineDetectionDataManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.JobDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.task.TaskInfo;
import org.apache.pinot.thirdeye.spi.task.TaskStatus;
import org.apache.pinot.thirdeye.task.MonitorTaskInfo;
import org.apache.pinot.thirdeye.task.TaskContext;
import org.apache.pinot.thirdeye.task.TaskResult;
import org.apache.pinot.thirdeye.task.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MonitorTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(MonitorJobRunner.class);
  private static final long MAX_TASK_TIME = TimeUnit.HOURS.toMillis(6);
  private static final long MAX_FAILED_DISABLE_DAYS = 30;

  private final TaskManager taskManager;
  private final JobManager jobManager;
  private final AlertManager alertManager;
  private final DetectionStatusManager detectionStatusManager;
  private final EvaluationManager evaluationManager;
  private final OnlineDetectionDataManager onlineDetectionDataManager;
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;

  @Inject
  public MonitorTaskRunner(final TaskManager taskManager,
      final JobManager jobManager,
      final AlertManager alertManager,
      final DetectionStatusManager detectionStatusManager,
      final EvaluationManager evaluationManager,
      final OnlineDetectionDataManager onlineDetectionDataManager,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    this.taskManager = taskManager;
    this.jobManager = jobManager;
    this.alertManager = alertManager;
    this.detectionStatusManager = detectionStatusManager;
    this.evaluationManager = evaluationManager;
    this.onlineDetectionDataManager = onlineDetectionDataManager;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
  }

  @Override
  public List<TaskResult> execute(TaskInfo taskInfo, TaskContext taskContext) {

    MonitorTaskInfo monitorTaskInfo = (MonitorTaskInfo) taskInfo;
    MonitorType monitorType = monitorTaskInfo.getMonitorType();
    if (monitorType.equals(MonitorType.UPDATE)) {
      executeMonitorUpdate(monitorTaskInfo);
    } else if (monitorType.equals(MonitorType.EXPIRE)) {
      executeMonitorExpire(monitorTaskInfo);
    } else {
      throw new UnsupportedOperationException(
          "Monitor task must be of type UPDATE/EXPIRE, found " + monitorType);
    }
    return null;
  }

  private void executeMonitorUpdate(MonitorTaskInfo monitorTaskInfo) {
    LOG.info("Execute monitor update {}", monitorTaskInfo);
    int jobRetentionDays = monitorTaskInfo.getDefaultRetentionDays();
    try {
      // Mark expired tasks with RUNNING states as TIMEOUT
      List<TaskDTO> timeoutTasks = taskManager
          .findTimeoutTasksWithinDays(jobRetentionDays, MAX_TASK_TIME);
      if (!timeoutTasks.isEmpty()) {
        for (TaskDTO task : timeoutTasks) {
          taskManager
              .updateStatusAndTaskEndTime(task.getId(), TaskStatus.RUNNING, TaskStatus.TIMEOUT,
                  System.currentTimeMillis(), "TIMEOUT status updated by MonitorTaskRunner");
        }
        LOG.warn("TIMEOUT tasks {}", timeoutTasks);
      }

      // Find all jobs in SCHEDULED status
      Map<Long, JobDTO> scheduledJobs = findScheduledJobsWithinDays(jobRetentionDays);

      // Remove SCHEDULED jobs that has WAITING tasks
      Set<Long> waitingJobs = findWaitingJobsWithinDays(jobRetentionDays);
      scheduledJobs.keySet().removeAll(waitingJobs);

      // Mark SCHEDULED jobs as TIMEOUT if it has any tasks that run for more than MAX_TASK_TIME or are marked as TIMEOUT
      Set<Long> timeoutJobs = findTimeoutJobsWithinDays(jobRetentionDays);
      if (!timeoutJobs.isEmpty()) {
        List<JobDTO> jobsToUpdate = extractJobDTO(scheduledJobs, timeoutJobs);
        if (!jobsToUpdate.isEmpty()) {
          jobManager.updateJobStatusAndEndTime(jobsToUpdate, JobStatus.TIMEOUT,
              System.currentTimeMillis());
          scheduledJobs.keySet().removeAll(timeoutJobs);
          LOG.info("TIMEOUT jobs {}", timeoutJobs);
        }
      }

      // Mark SCHEDULED jobs as FAILED if it has any tasks are marked as FAILED
      Set<Long> failedJobs = findFailedJobsWithinDays(jobRetentionDays);
      if (!failedJobs.isEmpty()) {
        List<JobDTO> jobsToUpdate = extractJobDTO(scheduledJobs, failedJobs);
        if (!jobsToUpdate.isEmpty()) {
          jobManager.updateJobStatusAndEndTime(jobsToUpdate, JobStatus.FAILED,
              System.currentTimeMillis());
          scheduledJobs.keySet().removeAll(failedJobs);
          LOG.info("FAILED jobs {}", timeoutJobs);
        }
      }

      // Mark the remaining jobs as COMPLETED
      if (!scheduledJobs.isEmpty()) {
        List<JobDTO> jobsToUpdate = new ArrayList<>(scheduledJobs.values());
        if (!jobsToUpdate.isEmpty()) {
          jobManager.updateJobStatusAndEndTime(jobsToUpdate, JobStatus.COMPLETED,
              System.currentTimeMillis());
          LOG.info("COMPLETED jobs {}", scheduledJobs.keySet());
        }
      }

      // disable alerts that failed consecutively for a long time
      disableLongFailedAlerts();
    } catch (Exception e) {
      LOG.error("Exception in monitor update task", e);
    }
  }

  /**
   * Disable the alert if it was updated before {@MAX_TASK_FAIL_DAYS} but there is no success run
   * since then.
   */
  private void disableLongFailedAlerts() {
    List<AlertDTO> detectionConfigs = alertManager.findAllActive();
    long currentTimeMillis = System.currentTimeMillis();
    long maxTaskFailMillis = TimeUnit.DAYS.toMillis(MAX_FAILED_DISABLE_DAYS);
    for (AlertDTO config : detectionConfigs) {
      try {
        Timestamp updateTime = config.getUpdateTime();
        if (updateTime != null && config.getHealth() != null
            && config.getHealth().getDetectionTaskStatus() != null) {
          long lastTaskExecutionTime = config.getHealth().getDetectionTaskStatus()
              .getLastTaskExecutionTime();
          // lastTaskExecutionTime == -1L is used for backward compatibility. Currently we have many long failing alerts have -1L.
          if (updateTime.getTime() <= currentTimeMillis - maxTaskFailMillis && (
              lastTaskExecutionTime == -1L
                  || lastTaskExecutionTime <= currentTimeMillis - maxTaskFailMillis)) {
            config.setActive(false);
            alertManager.update(config);
            LOG.info("Disabled alert {} since it failed more than {} days. "
                    + "Task last update time: {}. Last success task execution time: {}",
                config.getId(), MAX_FAILED_DISABLE_DAYS, config.getUpdateTime(),
                lastTaskExecutionTime);
          }
        }
      } catch (Exception e) {
        LOG.error("Exception in disabling alert ", e);
      }
    }
  }

  private void executeMonitorExpire(MonitorTaskInfo monitorTaskInfo) {
    LOG.info("Execute monitor expire {}", monitorTaskInfo);

    // Delete completed jobs and tasks that are expired.
    try {
      // CAUTION: Fist delete tasks then jobs, as task has a foreign key.
      int completedJobRetentionDays = monitorTaskInfo.getCompletedJobRetentionDays();
      int deletedCompletedTasks = taskManager
          .deleteRecordsOlderThanDaysWithStatus(completedJobRetentionDays, TaskStatus.COMPLETED);
      int deletedCompletedJobs =
          jobManager
              .deleteRecordsOlderThanDaysWithStatus(completedJobRetentionDays, JobStatus.COMPLETED);
      LOG.info("Deleted {} completed jobs and {} completed tasks that are older than {} days.",
          deletedCompletedJobs,
          deletedCompletedTasks, completedJobRetentionDays);
    } catch (Exception e) {
      LOG.error("Exception when expiring jobs and tasks.", e);
    }

    // Delete all types of jobs and tasks that are expired.
    try {
      // CAUTION: Fist delete tasks then jobs, as task has a foreign key.
      int jobRetentionDays = monitorTaskInfo.getDefaultRetentionDays();
      int deletedTasks = taskManager.deleteRecordsOlderThanDays(jobRetentionDays);
      int deletedJobs = jobManager.deleteRecordsOlderThanDays(jobRetentionDays);
      LOG.info("Deleted {} jobs and {} tasks that are older than {} days.", deletedTasks,
          deletedJobs,
          jobRetentionDays);
    } catch (Exception e) {
      LOG.error("Exception when expiring jobs and tasks.", e);
    }

    // Delete expired detection status.
    try {
      int deletedDetectionStatus = detectionStatusManager
          .deleteRecordsOlderThanDays(monitorTaskInfo.getDetectionStatusRetentionDays());
      LOG.info("Deleted {} detection status that are older than {} days.", deletedDetectionStatus,
          monitorTaskInfo.getDetectionStatusRetentionDays());
    } catch (Exception e) {
      LOG.error("Exception when expiring detection status.", e);
    }

    // Delete old evaluations.
    try {
      int deletedEvaluations = evaluationManager
          .deleteRecordsOlderThanDays(monitorTaskInfo.getDefaultRetentionDays());
      LOG.info("Deleted {} evaluations that are older than {} days.", deletedEvaluations,
          monitorTaskInfo.getDefaultRetentionDays());
    } catch (Exception e) {
      LOG.error("Exception when deleting old evaluations.", e);
    }

    // Delete expired online detection data
    try {
      int deletedOnlineDetectionDatas = onlineDetectionDataManager
          .deleteRecordsOlderThanDays(monitorTaskInfo.getDefaultRetentionDays());
      LOG.info("Deleted {} online detection data that are older than {} days.",
          deletedOnlineDetectionDatas, monitorTaskInfo.getDefaultRetentionDays());
    } catch (Exception e) {
      LOG.error("Exception when deleting old online detection data.", e);
    }

    // Delete old anomaly subscription notifications.
    try {
      int deletedRecords = anomalySubscriptionGroupNotificationManager
          .deleteRecordsOlderThanDays(monitorTaskInfo.getDefaultRetentionDays());
      LOG.info("Deleted {} anomaly subscription notifications that are older than {} days.",
          deletedRecords,
          monitorTaskInfo.getDefaultRetentionDays());
    } catch (Exception e) {
      LOG.error("Exception when deleting old anomaly subscription notifications.", e);
    }
  }

  private Map<Long, JobDTO> findScheduledJobsWithinDays(int days) {
    Map<Long, JobDTO> jobs = new HashMap<>();
    List<JobDTO> jobList = jobManager
        .findByStatusWithinDays(JobStatus.SCHEDULED, days);
    if (CollectionUtils.isNotEmpty(jobList)) {
      for (JobDTO jobDTO : jobList) {
        jobs.put(jobDTO.getId(), jobDTO);
      }
    }
    return jobs;
  }

  private Set<Long> findWaitingJobsWithinDays(int days) {
    Set<Long> waitingJobIds = new HashSet<>();
    List<TaskDTO> waitingTasks = taskManager
        .findByStatusWithinDays(TaskStatus.WAITING, days);
    for (TaskDTO task : waitingTasks) {
      waitingJobIds.add(task.getJobId());
    }
    return waitingJobIds;
  }

  private Set<Long> findTimeoutJobsWithinDays(int days) {
    Set<Long> timeoutJobs = new HashSet<>();
    List<TaskDTO> timeoutTasks = taskManager
        .findByStatusWithinDays(TaskStatus.TIMEOUT, days);
    for (TaskDTO task : timeoutTasks) {
      timeoutJobs.add(task.getJobId());
    }
    return timeoutJobs;
  }

  private Set<Long> findFailedJobsWithinDays(int days) {
    Set<Long> failedJobIds = new HashSet<>();
    List<TaskDTO> failedTasks = taskManager
        .findByStatusWithinDays(TaskStatus.FAILED, days);
    for (TaskDTO task : failedTasks) {
      failedJobIds.add(task.getJobId());
    }
    return failedJobIds;
  }

  private List<JobDTO> extractJobDTO(Map<Long, JobDTO> allJobs, Set<Long> jobIdToExtract) {
    List<JobDTO> jobsToUpdate = new ArrayList<>();
    for (Long jobId : jobIdToExtract) {
      JobDTO jobDTO = allJobs.get(jobId);
      if (jobDTO != null) {
        jobsToUpdate.add(jobDTO);
      }
    }
    return jobsToUpdate;
  }
}
