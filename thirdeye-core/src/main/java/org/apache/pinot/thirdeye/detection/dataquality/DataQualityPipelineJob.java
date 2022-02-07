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

package org.apache.pinot.thirdeye.detection.dataquality;

import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.detection.TaskUtils;
import org.apache.pinot.thirdeye.scheduler.ThirdEyeAbstractJob;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.task.TaskType;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The data quality job submitted to the scheduler. This job creates data quality tasks which
 * the runners will later pick and execute.
 */
// todo cyril dead because never launched in DetectionCronScheduler L140
public class DataQualityPipelineJob extends ThirdEyeAbstractJob {

  private static final Logger LOG = LoggerFactory.getLogger(DataQualityPipelineJob.class);

  private static final long DATA_AVAILABILITY_TASK_TIMEOUT = TimeUnit.MINUTES.toMillis(15);

  @Override
  public void execute(JobExecutionContext ctx) {
    DetectionPipelineTaskInfo taskInfo = TaskUtils.buildTaskInfo(ctx,
        getInstance(ctx, ThirdEyeCacheRegistry.class),
        getInstance(ctx, AlertManager.class),
        getInstance(ctx, DatasetConfigManager.class),
        getInstance(ctx, MetricConfigManager.class));

    // if a task is pending and not time out yet, don't schedule more
    String jobName = String
        .format("%s_%d", TaskType.DATA_QUALITY, taskInfo.getConfigId());
    final TaskManager taskManager = getInstance(ctx, TaskManager.class);
    if (TaskUtils.checkTaskAlreadyRun(jobName, taskInfo, DATA_AVAILABILITY_TASK_TIMEOUT,
        taskManager)) {
      LOG.info("Skip scheduling {} task for {} with start time {}. Task is already in the queue.",
          TaskType.DATA_QUALITY, jobName, taskInfo.getStart());
      return;
    }

    TaskUtils.createDataQualityTask(taskInfo, taskManager);
  }
}


