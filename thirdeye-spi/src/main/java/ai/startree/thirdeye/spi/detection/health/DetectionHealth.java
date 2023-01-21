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
package ai.startree.thirdeye.spi.detection.health;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * The detection health metric and status
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectionHealth {

  // overall health for a detection config
  @JsonProperty
  private HealthStatus overallHealth;

  // the regression metrics and status for a detection config
  @JsonProperty
  private RegressionStatus regressionStatus;

  // the anomaly coverage status for a detection config
  @JsonProperty
  private AnomalyCoverageStatus anomalyCoverageStatus;

  // the detection task status for a detection config
  @JsonProperty
  private DetectionTaskStatus detectionTaskStatus;

  /**
   * Create a unknown detection health
   *
   * @return the unknown detection health
   */
  public static DetectionHealth unknown() {
    DetectionHealth health = new DetectionHealth();
    health.anomalyCoverageStatus = AnomalyCoverageStatus.fromCoverageRatio(Double.NaN);
    health.detectionTaskStatus = DetectionTaskStatus.fromTasks(Collections.emptyList(), -1L);
    health.regressionStatus = RegressionStatus.fromDetectorMapes(Collections.emptyMap());
    health.overallHealth = HealthStatus.UNKNOWN;
    return health;
  }

  public HealthStatus getOverallHealth() {
    return overallHealth;
  }

  public RegressionStatus getRegressionStatus() {
    return regressionStatus;
  }

  public AnomalyCoverageStatus getAnomalyCoverageStatus() {
    return anomalyCoverageStatus;
  }

  public DetectionTaskStatus getDetectionTaskStatus() {
    return detectionTaskStatus;
  }

  /**
   * Builder for the detection health
   */
  public static class Builder {

    // database column name constants
    private static final String COL_NAME_START_TIME = "startTime";
    private static final String COL_NAME_END_TIME = "endTime";
    private static final String COL_NAME_DETECTION_CONFIG_ID = "detectionConfigId";
    private static final String COL_NAME_TASK_NAME = "name";
    private static final String COL_NAME_TASK_STATUS = "status";
    private static final String COL_NAME_TASK_TYPE = "type";
    private final long startTime;
    private final long endTime;
    private final long detectionConfigId;
    private MergedAnomalyResultManager anomalyDAO;
    private TaskManager taskDAO;
    // the number of task DTO returned in the detectionHealth
    private long taskLimit;
    private boolean provideOverallHealth;
    private DetectionHealth lastDetectionHealth;

    public Builder(long detectionConfigId, long startTime, long endTime) {
      Preconditions.checkArgument(endTime >= startTime, "end time must be after start time");
      this.startTime = startTime;
      this.endTime = endTime;
      this.detectionConfigId = detectionConfigId;
    }

    private static HealthStatus classifyOverallHealth(DetectionHealth health) {
      HealthStatus taskHealth = health.detectionTaskStatus.getHealthStatus();
      HealthStatus regressionHealth = health.regressionStatus.getHealthStatus();
      HealthStatus coverageHealth = health.anomalyCoverageStatus.getHealthStatus();

      Preconditions.checkNotNull(taskHealth);
      Preconditions.checkNotNull(regressionHealth);
      Preconditions.checkNotNull(coverageHealth);

      // if task fail ratio is high or both regression and coverage are bad, we say the overall status is bad
      if (taskHealth.equals(HealthStatus.BAD) || (regressionHealth.equals(HealthStatus.BAD)
          && coverageHealth.equals(
          HealthStatus.BAD))) {
        return HealthStatus.BAD;
      }

      Set<HealthStatus> statusSet = ImmutableSet.of(taskHealth, regressionHealth, coverageHealth);
      if (statusSet.contains(HealthStatus.MODERATE) || statusSet.contains(HealthStatus.BAD)) {
        return HealthStatus.MODERATE;
      }
      return HealthStatus.GOOD;
    }

    /**
     * Add the anomaly coverage health status in the health report built by the builder
     *
     * @param anomalyDAO the anomaly dao
     * @return the builder
     */
    public Builder addAnomalyCoverageStatus(MergedAnomalyResultManager anomalyDAO) {
      this.anomalyDAO = anomalyDAO;
      return this;
    }

    /**
     * Add the detection task health status in the health report built by the builder
     *
     * @param taskDAO the task dao
     * @param limit the maximum number of tasks returned in the health report (ordered by task
     *     start time, latest task first)
     * @return the builder
     */
    public Builder addDetectionTaskStatus(TaskManager taskDAO, long limit) {
      this.taskDAO = taskDAO;
      this.taskLimit = limit;
      return this;
    }

    /**
     * Add the detection task health status in the health report built by the builder. Do not return
     * any task details
     *
     * @param taskDAO the task dao
     * @return the builder
     */
    public Builder addDetectionTaskStatus(TaskManager taskDAO) {
      this.taskDAO = taskDAO;
      this.taskLimit = 0;
      return this;
    }

    /**
     * Add the global health status in the report built by the builder, consider both regression
     * health, coverage ratio and task health.
     * The overall health can be generated only if regression health, coverage ratio and task health
     * are available.
     *
     * @return the builder
     */
    public Builder addOverallHealth() {
      this.provideOverallHealth = true;
      return this;
    }

    /**
     * Add the original detection health. This is needed since we need to keep the last task success
     * time.
     *
     * @return the builder
     */
    public Builder addOriginalDetectionHealth(DetectionHealth lastDetectionHealth) {
      this.lastDetectionHealth = lastDetectionHealth;
      return this;
    }

    /**
     * Build the health status object
     *
     * @return the health status object
     */
    public DetectionHealth build() {
      DetectionHealth health = new DetectionHealth();
      if (this.anomalyDAO != null) {
        health.anomalyCoverageStatus = buildAnomalyCoverageStatus();
      }
      if (this.taskDAO != null) {
        health.detectionTaskStatus = buildTaskStatus();
      }
      if (this.provideOverallHealth) {
        health.overallHealth = classifyOverallHealth(health);
      }
      return health;
    }

    private AnomalyCoverageStatus buildAnomalyCoverageStatus() {
      // fetch anomalies
      List<AnomalyDTO> anomalies = this.anomalyDAO.findByPredicate(
          Predicate.AND(Predicate.LT(COL_NAME_START_TIME, this.endTime),
              Predicate.GT(COL_NAME_END_TIME, this.startTime),
              Predicate.EQ(COL_NAME_DETECTION_CONFIG_ID, detectionConfigId)));
      anomalies = anomalies.stream().filter(anomaly -> !anomaly.isChild())
          .collect(Collectors.toList());

      // the anomalies can come from different sub-dimensions, merge the anomaly range if possible
      List<Interval> intervals = new ArrayList<>();
      if (!anomalies.isEmpty()) {
        anomalies.sort(Comparator.comparingLong(AnomalyDTO::getStartTime));
        long start = Math.max(anomalies.stream().findFirst().get().getStartTime(), this.startTime);
        long end = anomalies.stream().findFirst().get().getEndTime();
        for (AnomalyDTO anomaly : anomalies) {
          if (anomaly.getStartTime() <= end) {
            end = Math.max(end, anomaly.getEndTime());
          } else {
            intervals.add(new Interval(start, end, DateTimeZone.UTC));
            start = anomaly.getStartTime();
            end = anomaly.getEndTime();
          }
        }
        intervals.add(new Interval(start, Math.min(end, this.endTime)));
      }

      // compute coverage
      long totalAnomalyCoverage =
          intervals.stream().map(interval -> interval.getEndMillis() - interval.getStartMillis())
              .reduce(0L, Long::sum);
      double coverageRatio = (double) totalAnomalyCoverage / (this.endTime - this.startTime);
      return AnomalyCoverageStatus.fromCoverageRatio(coverageRatio);
    }

    private DetectionTaskStatus buildTaskStatus() {
      // fetch tasks
      List<TaskDTO> tasks = this.taskDAO.findByPredicate(
          Predicate.AND(Predicate.EQ(COL_NAME_TASK_NAME, "DETECTION_" + this.detectionConfigId),
              Predicate.LT(COL_NAME_START_TIME, endTime),
              Predicate.GT(COL_NAME_END_TIME, startTime),
              Predicate.EQ(COL_NAME_TASK_TYPE, TaskType.DETECTION.toString()),
              Predicate.IN(COL_NAME_TASK_STATUS,
                  new String[]{TaskStatus.COMPLETED.toString(),
                      TaskStatus.FAILED.toString(),
                      TaskStatus.TIMEOUT.toString(),
                      TaskStatus.WAITING.toString()})));
      long lastTaskExecutionTime = -1L;
      if (lastDetectionHealth != null && lastDetectionHealth.getDetectionTaskStatus() != null) {
        lastTaskExecutionTime = lastDetectionHealth.getDetectionTaskStatus()
            .getLastTaskExecutionTime();
      }
      return DetectionTaskStatus.fromTasks(tasks, lastTaskExecutionTime, this.taskLimit);
    }
  }
}
