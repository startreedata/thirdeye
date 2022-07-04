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
