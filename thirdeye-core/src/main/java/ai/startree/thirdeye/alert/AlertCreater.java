/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.detection.TaskUtils.createTaskDto;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.mapper.AlertApiBeanMapper;
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

  @Inject
  public AlertCreater(
      final AlertManager alertManager,
      final AlertApiBeanMapper alertApiBeanMapper,
      final TaskManager taskDAO) {
    this.alertManager = alertManager;
    this.alertApiBeanMapper = alertApiBeanMapper;
    this.taskDAO = taskDAO;
  }

  public AlertDTO create(AlertApi api) {
    ensure(alertManager
        .findByPredicate(Predicate.EQ("name", api.getName()))
        .isEmpty(), ERR_DUPLICATE_NAME);

    final AlertDTO dto = alertApiBeanMapper.toAlertDTO(api);

    final Long id = alertManager.save(dto);
    dto.setId(id);

    createOnboardingTask(dto);
    return dto;
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
      TaskDTO taskDTO = createTaskDto(alertDTO.getId(), info, TaskType.ONBOARDING);
      final long taskId = taskDAO.save(taskDTO);
      LOG.info("Created {} task {} with settings {}", TaskType.ONBOARDING, taskId, taskDTO);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Error while serializing %s: %s",
          OnboardingTaskInfo.class.getSimpleName(), info), e);
    }
  }
}
