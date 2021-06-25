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
 *
 */

package org.apache.pinot.thirdeye.detection.onboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.apache.pinot.thirdeye.detection.DetectionPipeline;
import org.apache.pinot.thirdeye.detection.DetectionPipelineContext;
import org.apache.pinot.thirdeye.detection.DetectionPipelineFactory;
import org.apache.pinot.thirdeye.detection.DetectionPipelineResultV1;
import org.apache.pinot.thirdeye.detection.anomaly.task.TaskContext;
import org.apache.pinot.thirdeye.detection.anomaly.task.TaskResult;
import org.apache.pinot.thirdeye.detection.anomaly.task.TaskRunner;
import org.apache.pinot.thirdeye.detection.yaml.DetectionConfigTuner;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResultSource;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.task.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The task runner to run yaml onboarding task after a new detection is set up
 * It will replay the detection pipeline and the re-tune the pipeline.
 * Because for some pipeline component, tuning is depend on replay result
 */
@Singleton
public class YamlOnboardingTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(YamlOnboardingTaskRunner.class);

  private final AlertManager detectionDAO;
  private final MergedAnomalyResultManager anomalyDAO;
  private final DetectionPipelineFactory loader;
  private final DataProvider provider;

  @Inject
  public YamlOnboardingTaskRunner(final DataProvider provider,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager,
      final DetectionPipelineFactory loader) {
    this.loader = loader;
    this.detectionDAO = detectionConfigManager;
    this.anomalyDAO = mergedAnomalyResultManager;
    this.provider = provider;
  }

  @Override
  public List<TaskResult> execute(TaskInfo taskInfo, TaskContext taskContext) throws Exception {
    YamlOnboardingTaskInfo info = (YamlOnboardingTaskInfo) taskInfo;
    LOG.info("Running yaml detection onboarding task for id {}", info.getConfigId());

    // replay the detection pipeline
    AlertDTO config = this.detectionDAO.findById(info.getConfigId());
    if (config == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve config id %d", info.getConfigId()));
    }

    DetectionPipeline pipeline = this.loader.get(new DetectionPipelineContext()
        .setAlert(config)
        .setStart(info.getStart())
        .setEnd(info.getEnd()));
    DetectionPipelineResultV1 result = pipeline.run();

    if (result.getLastTimestamp() < 0) {
      return Collections.emptyList();
    }

    config.setLastTimestamp(result.getLastTimestamp());
    this.detectionDAO.update(config);

    for (MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      anomaly.setAnomalyResultSource(AnomalyResultSource.ANOMALY_REPLAY);
      this.anomalyDAO.save(anomaly);
      if (anomaly.getId() == null) {
        LOG.warn("Could not store anomaly:\n{}", anomaly);
      }
    }

    // re-tune the detection pipeline because tuning is depend on replay result. e.g. algorithm-based alert filter
    DetectionConfigTuner detectionConfigTuner = new DetectionConfigTuner(config, provider);
    AlertDTO tunedConfig = detectionConfigTuner
        .tune(info.getTuningWindowStart(), info.getTuningWindowEnd());
    this.detectionDAO.save(tunedConfig);

    LOG.info("Yaml detection onboarding task for id {} completed", info.getConfigId());
    return Collections.emptyList();
  }
}
