/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer;

import static ai.startree.thirdeye.spi.Constants.SCALING_FACTOR;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.OnboardDatasetMetricDTO;
import ai.startree.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RootCauseSessionDTO;
import ai.startree.thirdeye.spi.metric.MetricType;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DatalayerTestUtils {

  public static MetricConfigDTO getTestMetricConfig(String collection, String metric, Long id) {
    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    if (id != null) {
      metricConfigDTO.setId(id);
    }
    metricConfigDTO.setDataset(collection);
    metricConfigDTO.setDatatype(MetricType.LONG);
    metricConfigDTO.setName(metric);
    metricConfigDTO.setActive(Boolean.TRUE);
    metricConfigDTO.setAlias(SpiUtils.constructMetricAlias(collection, metric));
    return metricConfigDTO;
  }

  public static DatasetConfigDTO getTestDatasetConfig(String collection) {
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(collection);
    datasetConfigDTO.setDimensions(Lists.newArrayList("country", "browser", "environment"));
    datasetConfigDTO.setTimeColumn("time");
    datasetConfigDTO.setTimeDuration(1);
    datasetConfigDTO.setTimeUnit(TimeUnit.HOURS);
    datasetConfigDTO.setActive(true);
    datasetConfigDTO.setDataSource("PinotThirdEyeDataSource");
    datasetConfigDTO.setLastRefreshTime(System.currentTimeMillis());
    return datasetConfigDTO;
  }

  public static JobDTO getTestJobSpec() {
    JobDTO jobSpec = new JobDTO();
    jobSpec.setJobName("Test_Anomaly_Job");
    jobSpec.setStatus(Constants.JobStatus.SCHEDULED);
    jobSpec.setTaskType(TaskType.DETECTION);
    jobSpec.setScheduleStartTime(System.currentTimeMillis());
    jobSpec.setWindowStartTime(new DateTime(DateTimeZone.UTC).minusHours(20).getMillis());
    jobSpec.setWindowEndTime(new DateTime(DateTimeZone.UTC).minusHours(10).getMillis());
    jobSpec.setConfigId(100);
    return jobSpec;
  }

  public static DetectionStatusDTO getTestDetectionStatus(String dataset, long dateToCheckInMS,
      String dateToCheckInSDF, boolean detectionRun, long functionId) {
    DetectionStatusDTO detectionStatusDTO = new DetectionStatusDTO();
    detectionStatusDTO.setDataset(dataset);
    detectionStatusDTO.setFunctionId(functionId);
    detectionStatusDTO.setDateToCheckInMS(dateToCheckInMS);
    detectionStatusDTO.setDateToCheckInSDF(dateToCheckInSDF);
    detectionStatusDTO.setDetectionRun(detectionRun);
    return detectionStatusDTO;
  }

  public static EntityToEntityMappingDTO getTestEntityToEntityMapping(String fromURN, String toURN,
      String mappingType) {
    EntityToEntityMappingDTO dto = new EntityToEntityMappingDTO();
    dto.setFromURN(fromURN);
    dto.setToURN(toURN);
    dto.setMappingType(mappingType);
    dto.setScore(1);
    return dto;
  }

  public static OnboardDatasetMetricDTO getTestOnboardConfig(String datasetName, String metricName,
      String dataSource) {
    OnboardDatasetMetricDTO dto = new OnboardDatasetMetricDTO();
    dto.setDatasetName(datasetName);
    dto.setMetricName(metricName);
    dto.setDataSource(dataSource);
    return dto;
  }

  public static OverrideConfigDTO getTestOverrideConfigForTimeSeries(DateTime now) {
    OverrideConfigDTO overrideConfigDTO = new OverrideConfigDTO();
    overrideConfigDTO.setStartTime(now.minusHours(8).getMillis());
    overrideConfigDTO.setEndTime(now.plusHours(8).getMillis());
    overrideConfigDTO.setTargetEntity(OverrideConfigManager.ENTITY_TIME_SERIES);
    overrideConfigDTO.setActive(true);

    Map<String, String> overrideProperties = new HashMap<>();
    overrideProperties.put(SCALING_FACTOR, "1.2");
    overrideConfigDTO.setOverrideProperties(overrideProperties);

    Map<String, List<String>> overrideTarget = new HashMap<>();
    overrideTarget
        .put(OverrideConfigManager.TARGET_COLLECTION, Arrays.asList("collection1", "collection2"));
    overrideTarget.put(OverrideConfigManager.EXCLUDED_COLLECTION, Arrays.asList("collection3"));
    overrideConfigDTO.setTargetLevel(overrideTarget);

    return overrideConfigDTO;
  }

  public static RootCauseSessionDTO getTestRootcauseSessionResult(long start, long end,
      long created, long updated,
      String name, String owner, String text, String granularity, String compareMode,
      Long previousId, Long anomalyId) {
    RootCauseSessionDTO session = new RootCauseSessionDTO();
    session.setAnomalyRangeStart(start);
    session.setAnomalyRangeEnd(end);
    session.setAnalysisRangeStart(start - 100);
    session.setAnalysisRangeEnd(end + 100);
    session.setName(name);
    session.setOwner(owner);
    session.setText(text);
    session.setPreviousId(previousId);
    session.setAnomaly(new AnomalyApi().setId(anomalyId));
    session.setCreated(created);
    session.setUpdated(updated);
    session.setGranularity(granularity);
    session.setCompareMode(compareMode);
    return session;
  }
}
