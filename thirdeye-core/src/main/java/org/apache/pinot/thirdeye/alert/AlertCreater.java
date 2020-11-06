package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.CoreConstants.ONBOARDING_REPLAY_LOOKBACK;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.anomaly.task.TaskConstants;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.datalayer.util.Predicate;
import org.apache.pinot.thirdeye.detection.TaskUtils;
import org.apache.pinot.thirdeye.detection.onboard.YamlOnboardingTaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertCreater {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertCreater.class);

  private final AlertManager alertManager;
  private final AlertApiBeanMapper alertApiBeanMapper;
  private final TaskManager taskManager;

  @Inject
  public AlertCreater(
      final AlertManager alertManager,
      final AlertApiBeanMapper alertApiBeanMapper,
      final TaskManager taskManager) {
    this.alertManager = alertManager;
    this.alertApiBeanMapper = alertApiBeanMapper;
    this.taskManager = taskManager;
  }

  public AlertDTO create(AlertApi api) {
    ensure(alertManager
        .findByPredicate(Predicate.EQ("name", api.getName()))
        .isEmpty(), ERR_DUPLICATE_NAME);

    final AlertDTO dto = alertApiBeanMapper.toAlertDTO(api);

    final Long id = alertManager.save(dto);
    dto.setId(id);

    createOnboardingTask(dto, 0, 0);
    return dto;
  }

  private void createOnboardingTask(
      final AlertDTO alertDTO,
      long tuningWindowStart,
      long tuningWindowEnd
  ) {
    YamlOnboardingTaskInfo info = new YamlOnboardingTaskInfo();
    info.setConfigId(alertDTO.getId());
    if (tuningWindowStart == 0L && tuningWindowEnd == 0L) {
      // default tuning window 28 days
      tuningWindowEnd = System.currentTimeMillis();
      tuningWindowStart = tuningWindowEnd - TimeUnit.DAYS.toMillis(28);
    }
    info.setTuningWindowStart(tuningWindowStart);
    info.setTuningWindowEnd(tuningWindowEnd);
    info.setEnd(System.currentTimeMillis());

    long lastTimestamp = alertDTO.getLastTimestamp();
    // If no value is present, set the default lookback
    if (lastTimestamp < 0) {
      lastTimestamp = info.getEnd() - ONBOARDING_REPLAY_LOOKBACK;
    }
    info.setStart(lastTimestamp);

    String taskInfoJson;
    try {
      taskInfoJson = new ObjectMapper().writeValueAsString(info);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Error while serializing %s: %s",
          YamlOnboardingTaskInfo.class.getSimpleName(), info), e);
    }

    TaskDTO taskDTO = TaskUtils.buildTask(alertDTO.getId(), taskInfoJson,
        TaskConstants.TaskType.YAML_DETECTION_ONBOARD);
    long taskId = taskManager.save(taskDTO);
    LOG.info("Created {} task {} with taskId {}", TaskConstants.TaskType.YAML_DETECTION_ONBOARD,
        taskDTO, taskId);
  }
}
