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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.DetectionPipeline;
import org.apache.pinot.thirdeye.detection.DetectionPipelineContext;
import org.apache.pinot.thirdeye.detection.DetectionPipelineFactory;
import org.apache.pinot.thirdeye.detection.DetectionPipelineResultV1;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.task.TaskInfo;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.apache.pinot.thirdeye.task.TaskContext;
import org.apache.pinot.thirdeye.task.TaskResult;
import org.apache.pinot.thirdeye.task.TaskRunner;
import org.apache.pinot.thirdeye.util.ThirdeyeMetricsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ths class is responsible for running the data quality tasks
 */
@Singleton
public class DataQualityPipelineTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DataQualityPipelineTaskRunner.class);

  private final AlertManager alertManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final DetectionPipelineFactory detectionPipelineFactory;

  /**
   * Default constructor for ThirdEye task execution framework.
   * Loads dependencies from DAORegitry and CacheRegistry
   */
  @Inject
  public DataQualityPipelineTaskRunner(final DetectionPipelineFactory detectionPipelineFactory,
      final AlertManager detectionConfigManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.detectionPipelineFactory = detectionPipelineFactory;
    this.alertManager = detectionConfigManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  @Override
  public List<TaskResult> execute(TaskInfo taskInfo, TaskContext taskContext) throws Exception {
    ThirdeyeMetricsUtil.dataQualityTaskCounter.inc();

    try {
      DetectionPipelineTaskInfo info = (DetectionPipelineTaskInfo) taskInfo;
      AlertDTO config = this.alertManager.findById(info.getConfigId());
      if (config == null) {
        throw new IllegalArgumentException(
            String.format("Could not resolve config id %d", info.getConfigId()));
      }

      LOG.info("Start data quality check for config {} between {} and {}", config.getId(),
          info.getStart(), info.getEnd());
      Map<String, Object> props = config.getProperties();
      // A small hack to reuse the properties field to run the data quality pipeline; this is reverted after the run.
      config.setProperties(config.getDataQualityProperties());

      final DetectionPipeline pipeline = this.detectionPipelineFactory.get(new DetectionPipelineContext()
          .setAlert(config)
          .setStart(info.getStart())
          .setEnd(info.getEnd())
      );

      DetectionPipelineResultV1 result = pipeline.run();
      // revert the properties field back to detection properties
      config.setProperties(props);

      // Save all the data quality anomalies
      for (MergedAnomalyResultDTO mergedAnomalyResultDTO : result.getAnomalies()) {
        this.mergedAnomalyResultManager.save(mergedAnomalyResultDTO);
        if (mergedAnomalyResultDTO.getId() == null) {
          LOG.warn("Could not store anomaly:\n{}", mergedAnomalyResultDTO);
        }
      }

      ThirdeyeMetricsUtil.dataQualityTaskSuccessCounter.inc();
      LOG.info("End data quality check for config {} between {} and {}. Detected {} anomalies.",
          config.getId(),
          info.getStart(), info.getEnd(), result.getAnomalies());
      return Collections.emptyList();
    } catch (Exception e) {
      ThirdeyeMetricsUtil.dataQualityTaskExceptionCounter.inc();
      throw e;
    }
  }
}
