/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.scheduler;

import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import java.util.Set;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

/**
 * Interface for ThirdEye's scheduling components
 */
public interface ThirdEyeCronScheduler extends Runnable {

  String CRON_TIMEZONE = "UTC";

  void addToContext(String identifier, Object instance);

  // Initialize and prepare the scheduler
  void start() throws SchedulerException;

  // Safely bring down the scheduler
  void shutdown() throws SchedulerException;

  // Trigger the scheduler to start creating jobs.
  void startJob(AbstractDTO config, JobDetail key) throws SchedulerException;

  // Stop the scheduler from scheduling more jobs.
  void stopJob(JobKey key) throws SchedulerException;

  // Retrieve all the scheduled jobs
  Set<JobKey> getScheduledJobs() throws SchedulerException;

  // Get the key for the scheduling job
  String getJobKey(Long id, TaskType taskType);
}
