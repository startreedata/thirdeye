package org.apache.pinot.thirdeye.alert;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.pinot.thirdeye.detection.v2.components.filler.TimeIndexFiller.floorByPeriod;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.TaskUtils;
import org.apache.pinot.thirdeye.mapper.AlertApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.task.TaskType;
import org.apache.pinot.thirdeye.task.YamlOnboardingTaskInfo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertCreater {

  private static final Logger LOG = LoggerFactory.getLogger(AlertCreater.class);
  // default onboarding replay period
  private final static Period ONBOARDING_REPLAY_LOOKBACK = Period.days(180);

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

    createOnboardingTask(dto);
    return dto;
  }

  private void createOnboardingTask(final AlertDTO dto) {
    long end = System.currentTimeMillis();
    long start = dto.getLastTimestamp();
    // If no value is present, set the default lookback
    if (start <= 0) {
      start = getDefaultReplayStart(end);
    }

    createOnboardingTask(dto, start, end);
  }

  private long getDefaultReplayStart(final long end) {
    DateTime endTime = new DateTime(end, DateTimeZone.UTC);
    // hack - detection granularity is unknown - Flooring to month to avoid incomplete bucket
    // incomplete buckets are not managed correctly for the moment
    // eg: lookback 60 days on may 28 -> startTime=march 29 --> value for march bucket is small
    // --> false alert for threshold detection - (no problem for other kind of alerts)
    return floorByPeriod(endTime.minus(ONBOARDING_REPLAY_LOOKBACK), Period.months(1)).getMillis();
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
    YamlOnboardingTaskInfo info = new YamlOnboardingTaskInfo();
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

    final String taskInfoJson;
    try {
      taskInfoJson = new ObjectMapper().writeValueAsString(info);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Error while serializing %s: %s",
          YamlOnboardingTaskInfo.class.getSimpleName(), info), e);
    }

    final TaskDTO taskDTO = TaskUtils.buildTask(alertDTO.getId(), taskInfoJson,
        TaskType.ONBOARDING);
    final long taskId = taskManager.save(taskDTO);
    LOG.info("Created {} task {} with taskId {}", TaskType.ONBOARDING,
        taskDTO, taskId);
  }
}
