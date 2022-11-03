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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.config.TimeConfiguration;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertCreater {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertCreater.class);
  private final AlertManager alertManager;
  private final TaskManager taskManager;
  private final AlertInsightsProvider alertInsightsProvider;
  private final long minimumOnboardingStartTime;

  @Inject
  public AlertCreater(final AlertManager alertManager,
      final TaskManager taskManager, final AlertInsightsProvider alertInsightsProvider,
      final TimeConfiguration timeConfiguration) {
    this.alertManager = alertManager;
    this.taskManager = taskManager;
    this.alertInsightsProvider = alertInsightsProvider;
    this.minimumOnboardingStartTime = timeConfiguration.getMinimumOnboardingStartTime();
  }

  public AlertDTO create(AlertApi api) {
    ensureCreationIsPossible(api);

    final AlertDTO dto = ApiBeanMapper.toAlertDto(api);

    final Long id = alertManager.save(dto);
    dto.setId(id);

    createOnboardingTask(dto);
    return dto;
  }

  public void ensureCreationIsPossible(final AlertApi api) {
    ensure(alertManager.findByPredicate(Predicate.EQ("name", api.getName())).isEmpty(),
        ERR_DUPLICATE_NAME);
  }

  public void createOnboardingTask(final AlertDTO dto) {
    long end = System.currentTimeMillis();
    long start = dto.getLastTimestamp();
    // If no value is present, set the default lookback
    if (start <= 0) {
      start = getDefaultStart(dto);
    }

    createOnboardingTask(dto, start, end);
  }

  private long getDefaultStart(final AlertDTO dto) {
    try {
      final AlertInsightsApi insights = alertInsightsProvider.getInsights(dto);
      final Long datasetStartTime = insights.getDatasetStartTime();
      if (datasetStartTime < minimumOnboardingStartTime) {
        LOG.warn(
            "Dataset start time {} is smaller than the minimum onboarding time allowed {}. Using the minimum time allowed.",
            datasetStartTime, minimumOnboardingStartTime);
        return minimumOnboardingStartTime;
      }
      return datasetStartTime;
    } catch (final WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      // replay from JAN 1 2000 because replaying from 1970 is too slow with small granularity
      LOG.error("Could not fetch insights for alert {}. Using the minimum time allowed. {}",
          dto,
          minimumOnboardingStartTime);
      return minimumOnboardingStartTime;
    }
  }

  public void createOnboardingTask(final AlertDTO alertDTO, final long start, final long end) {
    final DetectionPipelineTaskInfo info = new DetectionPipelineTaskInfo();
    info.setConfigId(alertDTO.getId());

    checkArgument(start <= end);
    info.setStart(start)
        .setEnd(end);

    try {
      TaskDTO taskDTO = taskManager.createTaskDto(alertDTO.getId(), info, TaskType.ONBOARDING);
      LOG.info("Created {} task {} with settings {}",
          TaskType.ONBOARDING,
          taskDTO.getId(),
          taskDTO);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Error while serializing %s: %s",
          DetectionPipelineTaskInfo.class.getSimpleName(),
          info), e);
    }
  }
}
