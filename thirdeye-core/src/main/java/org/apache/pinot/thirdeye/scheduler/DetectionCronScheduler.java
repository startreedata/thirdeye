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

package org.apache.pinot.thirdeye.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.DetectionPipelineJob;
import org.apache.pinot.thirdeye.detection.TaskUtils;
import org.apache.pinot.thirdeye.detection.anomaly.utils.AnomalyUtils;
import org.apache.pinot.thirdeye.detection.dataquality.DataQualityPipelineJob;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.task.TaskType;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionCronScheduler implements ThirdEyeCronScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionCronScheduler.class);

  public static final int DEFAULT_DETECTION_DELAY = 1;
  public static final TimeUnit DEFAULT_ALERT_DELAY_UNIT = TimeUnit.MINUTES;
  public static final String QUARTZ_DETECTION_GROUPER = TaskType.DETECTION.toString();

  final AlertManager detectionDAO;
  final Scheduler scheduler;
  final ScheduledExecutorService executorService;

  @Inject
  public DetectionCronScheduler(AlertManager detectionDAO) {
    this.detectionDAO = detectionDAO;
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    try {
      this.scheduler = StdSchedulerFactory.getDefaultScheduler();
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addToContext(final String identifier, final Object instance) {
    try {
      scheduler.getContext().put(identifier, instance);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void start() throws SchedulerException {
    this.scheduler.start();
    this.executorService
        .scheduleWithFixedDelay(this, 0, DEFAULT_DETECTION_DELAY, DEFAULT_ALERT_DELAY_UNIT);
  }

  @Override
  public void run() {
    try {
      Collection<AlertDTO> configs = this.detectionDAO.findAll();

      // add or update
      for (AlertDTO config : configs) {
        if (!config.isActive()) {
          LOG.debug("Detection config " + config.getId() + " is inactive. Skipping.");
          continue;
        }
        if (config.isDataAvailabilitySchedule()) {
          LOG.debug("Detection config " + config.getId()
              + " is enabled for data availability scheduling. Skipping.");
          continue;
        }

        try {
          // Schedule detection jobs
          JobKey detectionJobKey = new JobKey(
              getJobKey(config.getId(), TaskType.DETECTION),
              QUARTZ_DETECTION_GROUPER);
          JobDetail detectionJob = JobBuilder.newJob(DetectionPipelineJob.class)
              .withIdentity(detectionJobKey).build();
          if (scheduler.checkExists(detectionJobKey)) {
            LOG.info("Detection config " + detectionJobKey.getName()
                + " is already scheduled for detection");
            if (isJobUpdated(config, detectionJobKey)) {
              restartJob(config, detectionJob);
            }
          } else {
            startJob(config, detectionJob);
          }

          // Schedule data quality jobs
          JobKey dataQualityJobKey = new JobKey(
              getJobKey(config.getId(), TaskType.DATA_QUALITY),
              QUARTZ_DETECTION_GROUPER);
          JobDetail dataQualityJob = JobBuilder.newJob(DataQualityPipelineJob.class)
              .withIdentity(dataQualityJobKey).build();
          if (scheduler.checkExists(dataQualityJobKey)) {
            LOG.info("Detection config " + dataQualityJobKey.getName()
                + " is already scheduled for data quality");
            if (isJobUpdated(config, dataQualityJobKey)) {
              restartJob(config, dataQualityJob);
            }
          } else {
            // todo cyril dead because data quality is never enabled
            if (DetectionUtils.isDataQualityCheckEnabled(config)) {
              startJob(config, dataQualityJob);
            }
          }
        } catch (Exception e) {
          LOG.error("Error creating/updating job key for detection config {}", config.getId());
        }
      }

      Set<JobKey> scheduledJobs = getScheduledJobs();
      for (JobKey jobKey : scheduledJobs) {
        try {
          Long id = TaskUtils.getIdFromJobKey(jobKey.getName());
          AlertDTO detectionDTO = detectionDAO.findById(id);
          if (detectionDTO == null) {
            LOG.info("Found a scheduled detection config task, but not found in the database {}",
                id);
            stopJob(jobKey);
            continue;
          } else if (!detectionDTO.isActive()) {
            LOG.info("Found a scheduled detection config task, but has been deactivated {}", id);
            stopJob(jobKey);
            continue;
          }
          if (detectionDTO.isDataAvailabilitySchedule()) {
            LOG.info("Found a scheduled pipeline enabled for data availability scheduling.");
            stopJob(jobKey);
          }
        } catch (Exception e) {
          LOG.error("Error removing job key {}", jobKey);
        }
      }
    } catch (SchedulerException e) {
      LOG.error("Error while scheduling detection pipeline", e);
    }
  }

  private void restartJob(AlertDTO config, JobDetail job) throws SchedulerException {
    stopJob(job.getKey());
    startJob(config, job);
  }

  @Override
  public Set<JobKey> getScheduledJobs() throws SchedulerException {
    return this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(QUARTZ_DETECTION_GROUPER));
  }

  @Override
  public void shutdown() throws SchedulerException {
    AnomalyUtils.safelyShutdownExecutionService(executorService, this.getClass());
    scheduler.shutdown();
  }

  @Override
  public void startJob(AbstractDTO config, JobDetail job) throws SchedulerException {
    Trigger trigger = TriggerBuilder.newTrigger().withSchedule(
        CronScheduleBuilder.cronSchedule(((AlertDTO) config).getCron())).build();
    this.scheduler.scheduleJob(job, trigger);
    LOG.info(String.format("scheduled detection pipeline job %s", job.getKey().getName()));
  }

  @Override
  public void stopJob(JobKey jobKey) throws SchedulerException {
    if (!this.scheduler.checkExists(jobKey)) {
      throw new IllegalStateException(
          "Cannot stop detection pipeline " + jobKey.getName() + ", it has not been scheduled");
    }
    this.scheduler.deleteJob(jobKey);
    LOG.info("Stopped detection pipeline {}", jobKey.getName());
  }

  @Override
  public String getJobKey(Long id, TaskType taskType) {
    return String.format("%s_%d", taskType, id);
  }

  private boolean isJobUpdated(AlertDTO config, JobKey key) throws SchedulerException {
    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(key);
    CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
    String cronInSchedule = cronTrigger.getCronExpression();

    if (!config.getCron().equals(cronInSchedule)) {
      LOG.info("Cron expression for detection pipeline {} has been changed from {}  to {}. "
              + "Restarting schedule",
          config.getId(), cronInSchedule, config.getCron());
      return true;
    }
    return false;
  }
}
