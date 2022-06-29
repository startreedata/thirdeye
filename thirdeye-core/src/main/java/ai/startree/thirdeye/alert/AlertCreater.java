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

import ai.startree.thirdeye.mapper.AlertApiBeanMapper;
import ai.startree.thirdeye.scheduler.JobSchedulerService;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.OnboardingTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertCreater {

  // default onboarding replay period
  // not used - with the current implementation alert is replayed from epoch 0
  // to get this right, this should depend on the granularity
  private static final long ONBOARDING_REPLAY_LOOKBACK = TimeUnit.DAYS.toMillis(60);

  protected static final Logger LOG = LoggerFactory.getLogger(AlertCreater.class);

  private final AlertManager alertManager;
  private final AlertApiBeanMapper alertApiBeanMapper;
  private final TaskManager taskDAO;
  private final JobSchedulerService jobSchedulerService;

  @Inject
  public AlertCreater(
      final AlertManager alertManager,
      final AlertApiBeanMapper alertApiBeanMapper,
      final TaskManager taskDAO,
      final JobSchedulerService jobSchedulerService) {
    this.alertManager = alertManager;
    this.alertApiBeanMapper = alertApiBeanMapper;
    this.taskDAO = taskDAO;
    this.jobSchedulerService = jobSchedulerService;
  }

  public AlertDTO create(AlertApi api) {
    ensureCreationIsPossible(api);

    final AlertDTO dto = alertApiBeanMapper.toAlertDTO(api);

    final Long id = alertManager.save(dto);
    dto.setId(id);

    createOnboardingTask(dto);
    return dto;
  }

  public void ensureCreationIsPossible(final AlertApi api) {
    ensure(alertManager
        .findByPredicate(Predicate.EQ("name", api.getName()))
        .isEmpty(), ERR_DUPLICATE_NAME);
  }

  private void createOnboardingTask(final AlertDTO dto) {
    long end = System.currentTimeMillis();
    long start = dto.getLastTimestamp();
    // If no value is present, set the default lookback
    if (start < 0) {
      start = end - ONBOARDING_REPLAY_LOOKBACK;
    }

    createOnboardingTask(dto, start, end);
  }

  public void createOnboardingTask(final AlertDTO dto, final long start, final long end) {
    createOnboardingTask(dto, start, end, 0, 0);
  }

  private void createOnboardingTask(
      final AlertDTO alertDTO,
      long start,
      long end,
      long tuningWindowStart,
      long tuningWindowEnd
  ) {
    OnboardingTaskInfo info = new OnboardingTaskInfo();
    info.setConfigId(alertDTO.getId());
    if (tuningWindowStart == 0L && tuningWindowEnd == 0L) {
      // default tuning window 28 days
      tuningWindowEnd = System.currentTimeMillis();
      tuningWindowStart = tuningWindowEnd - TimeUnit.DAYS.toMillis(28);
    }

    checkArgument(start <= end);
    checkArgument(tuningWindowStart <= tuningWindowEnd);

    info
        .setTuningWindowStart(tuningWindowStart)
        .setTuningWindowEnd(tuningWindowEnd)
        .setStart(start)
        .setEnd(end)
    ;

    try {
      TaskDTO taskDTO = jobSchedulerService.createTaskDto(alertDTO.getId(), info, TaskType.ONBOARDING);
      final long taskId = taskDAO.save(taskDTO);
      LOG.info("Created {} task {} with settings {}", TaskType.ONBOARDING, taskId, taskDTO);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Error while serializing %s: %s",
          OnboardingTaskInfo.class.getSimpleName(), info), e);
    }
  }
}
