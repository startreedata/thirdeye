/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.scheduler.job;

import static ai.startree.thirdeye.scheduler.JobSchedulerService.getIdFromJobKey;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Detection alert job that run by the cron scheduler.
 * This job put notification task into database which can be picked up by works later.
 */
public class NotificationPipelineJob extends ThirdEyeAbstractJob {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationPipelineJob.class);

  @Override
  public void execute(final JobExecutionContext ctx) {
    String jobKey = "unknown";
    try {
      jobKey = ctx.getJobDetail().getKey().getName();
      final var instance = getInstance(ctx, NotificationPipelineTaskCreator.class);

      final long subscriptionGroupId = getIdFromJobKey(jobKey);
      instance.createTask(subscriptionGroupId);
    } catch (Exception e) {
      // Catch all exception to avoid job being stuck in the scheduler.
      LOG.error("Exception running job key:" + jobKey, e);
    }
  }
}
