/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.scheduler;

import ai.startree.thirdeye.detection.DetectionPipelineJob;
import ai.startree.thirdeye.detection.TaskUtils;
import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
  private Integer activeAlerts;

  @Inject
  public DetectionCronScheduler(
      final AlertManager detectionDAO,
      final MetricRegistry metricRegistry) {
    this.detectionDAO = detectionDAO;
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    metricRegistry.register("activeAlertsCount", new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return activeAlerts;
      }
    });
    try {
      this.scheduler = StdSchedulerFactory.getDefaultScheduler();
    } catch (final SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addToContext(final String identifier, final Object instance) {
    try {
      scheduler.getContext().put(identifier, instance);
    } catch (final SchedulerException e) {
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
      final Collection<AlertDTO> configs = this.detectionDAO.findAll();
      activeAlerts = detectionDAO.findAllActive().size();
      // add or update
      for (final AlertDTO config : configs) {
        if (!config.isActive()) {
          LOG.debug("Detection config " + config.getId() + " is inactive. Skipping.");
          continue;
        }

        try {
          // Schedule detection jobs
          final JobKey detectionJobKey = new JobKey(
              getJobKey(config.getId(), TaskType.DETECTION),
              QUARTZ_DETECTION_GROUPER);
          final JobDetail detectionJob = JobBuilder.newJob(DetectionPipelineJob.class)
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

        } catch (final Exception e) {
          LOG.error("Error creating/updating job key for detection config {}", config.getId());
        }
      }

      final Set<JobKey> scheduledJobs = getScheduledJobs();
      for (final JobKey jobKey : scheduledJobs) {
        try {
          final Long id = TaskUtils.getIdFromJobKey(jobKey.getName());
          final AlertDTO detectionDTO = detectionDAO.findById(id);
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
        } catch (final Exception e) {
          LOG.error("Error removing job key {}", jobKey);
        }
      }
    } catch (final SchedulerException e) {
      LOG.error("Error while scheduling detection pipeline", e);
    }
  }

  private void restartJob(final AlertDTO config, final JobDetail job) throws SchedulerException {
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
  public void startJob(final AbstractDTO config, final JobDetail job) throws SchedulerException {
    final Trigger trigger = TriggerBuilder.newTrigger().withSchedule(
        CronScheduleBuilder.cronSchedule(((AlertDTO) config).getCron())
            .inTimeZone(TimeZone.getTimeZone(CRON_TIMEZONE))).build();
    this.scheduler.scheduleJob(job, trigger);
    LOG.info(String.format("scheduled detection pipeline job %s", job.getKey().getName()));
  }

  @Override
  public void stopJob(final JobKey jobKey) throws SchedulerException {
    if (!this.scheduler.checkExists(jobKey)) {
      throw new IllegalStateException(
          "Cannot stop detection pipeline " + jobKey.getName() + ", it has not been scheduled");
    }
    this.scheduler.deleteJob(jobKey);
    LOG.info("Stopped detection pipeline {}", jobKey.getName());
  }

  @Override
  public String getJobKey(final Long id, final TaskType taskType) {
    return String.format("%s_%d", taskType, id);
  }

  private boolean isJobUpdated(final AlertDTO config, final JobKey key) throws SchedulerException {
    final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(key);
    final CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
    final String cronInSchedule = cronTrigger.getCronExpression();

    if (!config.getCron().equals(cronInSchedule)) {
      LOG.info("Cron expression for detection pipeline {} has been changed from {}  to {}. "
              + "Restarting schedule",
          config.getId(), cronInSchedule, config.getCron());
      return true;
    }
    return false;
  }
}
