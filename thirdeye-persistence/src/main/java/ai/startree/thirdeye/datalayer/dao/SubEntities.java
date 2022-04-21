/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.dao;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.AlertTemplateIndex;
import ai.startree.thirdeye.datalayer.entity.AnomalyFeedbackIndex;
import ai.startree.thirdeye.datalayer.entity.AnomalySubscriptionGroupNotificationIndex;
import ai.startree.thirdeye.datalayer.entity.DataSourceIndex;
import ai.startree.thirdeye.datalayer.entity.DatasetConfigIndex;
import ai.startree.thirdeye.datalayer.entity.DetectionAlertConfigIndex;
import ai.startree.thirdeye.datalayer.entity.DetectionConfigIndex;
import ai.startree.thirdeye.datalayer.entity.DetectionStatusIndex;
import ai.startree.thirdeye.datalayer.entity.EntityToEntityMappingIndex;
import ai.startree.thirdeye.datalayer.entity.EvaluationIndex;
import ai.startree.thirdeye.datalayer.entity.EventIndex;
import ai.startree.thirdeye.datalayer.entity.JobIndex;
import ai.startree.thirdeye.datalayer.entity.MergedAnomalyResultIndex;
import ai.startree.thirdeye.datalayer.entity.MetricConfigIndex;
import ai.startree.thirdeye.datalayer.entity.OnboardDatasetMetricIndex;
import ai.startree.thirdeye.datalayer.entity.OnlineDetectionDataIndex;
import ai.startree.thirdeye.datalayer.entity.OverrideConfigIndex;
import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.datalayer.entity.RootcauseTemplateIndex;
import ai.startree.thirdeye.datalayer.entity.SubEntityType;
import ai.startree.thirdeye.datalayer.entity.TaskIndex;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.OnboardDatasetMetricDTO;
import ai.startree.thirdeye.spi.datalayer.dto.OnlineDetectionDataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RootcauseTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.google.common.collect.ImmutableMap;

/**
 * ThirdEye entities consists of GenericJsonEntity and the index tables of the sub entities.
 * Sub Entities are the main business objects which are alerts, metrics, datasets, datasources, etc
 */
public class SubEntities {

  static final ImmutableMap<Class<? extends AbstractDTO>, Class<? extends AbstractIndexEntity>>
      BEAN_INDEX_MAP = buildBeanIndexMap();

  static final ImmutableMap<Class<? extends AbstractDTO>, SubEntityType>
      BEAN_TYPE_MAP = buildTypeBeanBimap();

  private static ImmutableMap<Class<? extends AbstractDTO>, Class<? extends AbstractIndexEntity>>
  buildBeanIndexMap() {
    return ImmutableMap.<Class<? extends AbstractDTO>, Class<? extends AbstractIndexEntity>>builder()
        .put(AlertDTO.class, DetectionConfigIndex.class)
        .put(AlertTemplateDTO.class, AlertTemplateIndex.class)
        .put(AnomalyFeedbackDTO.class, AnomalyFeedbackIndex.class)
        .put(AnomalySubscriptionGroupNotificationDTO.class,
            AnomalySubscriptionGroupNotificationIndex.class)
        .put(DataSourceDTO.class, DataSourceIndex.class)
        .put(DatasetConfigDTO.class, DatasetConfigIndex.class)
        .put(DetectionStatusDTO.class, DetectionStatusIndex.class)
        .put(EntityToEntityMappingDTO.class, EntityToEntityMappingIndex.class)
        .put(EvaluationDTO.class, EvaluationIndex.class)
        .put(EventDTO.class, EventIndex.class)
        .put(JobDTO.class, JobIndex.class)
        .put(MergedAnomalyResultDTO.class, MergedAnomalyResultIndex.class)
        .put(MetricConfigDTO.class, MetricConfigIndex.class)
        .put(OnboardDatasetMetricDTO.class, OnboardDatasetMetricIndex.class)
        .put(OnlineDetectionDataDTO.class, OnlineDetectionDataIndex.class)
        .put(OverrideConfigDTO.class, OverrideConfigIndex.class)
        .put(RcaInvestigationDTO.class, RcaInvestigationIndex.class)
        .put(RootcauseTemplateDTO.class, RootcauseTemplateIndex.class)
        .put(SubscriptionGroupDTO.class, DetectionAlertConfigIndex.class)
        .put(TaskDTO.class, TaskIndex.class)
        .build();
  }

  private static ImmutableMap<Class<? extends AbstractDTO>, SubEntityType> buildTypeBeanBimap() {
    return ImmutableMap.<Class<? extends AbstractDTO>, SubEntityType>builder()
        .put(AlertDTO.class, SubEntityType.ALERT)
        .put(AlertTemplateDTO.class, SubEntityType.ALERT_TEMPLATE)
        .put(AnomalyFeedbackDTO.class, SubEntityType.ANOMALY_FEEDBACK)
        .put(AnomalySubscriptionGroupNotificationDTO.class,
            SubEntityType.ANOMALY_SUBSCRIPTION_GROUP_NOTIFICATION)
        .put(DataSourceDTO.class, SubEntityType.DATA_SOURCE)
        .put(DatasetConfigDTO.class, SubEntityType.DATASET)
        .put(DetectionStatusDTO.class, SubEntityType.DETECTION_STATUS)
        .put(EntityToEntityMappingDTO.class, SubEntityType.ENTITY_TO_ENTITY_MAPPING)
        .put(EvaluationDTO.class, SubEntityType.EVALUATION)
        .put(EventDTO.class, SubEntityType.EVENT)
        .put(JobDTO.class, SubEntityType.JOB)
        .put(MergedAnomalyResultDTO.class, SubEntityType.ANOMALY)
        .put(MetricConfigDTO.class, SubEntityType.METRIC)
        .put(OnboardDatasetMetricDTO.class, SubEntityType.ONBOARD_DATASET_METRIC)
        .put(OnlineDetectionDataDTO.class, SubEntityType.ONLINE_DETECTION_DATA)
        .put(OverrideConfigDTO.class, SubEntityType.OVERRIDE_CONFIG)
        .put(RcaInvestigationDTO.class, SubEntityType.ROOT_CAUSE_SESSION)
        .put(RootcauseTemplateDTO.class, SubEntityType.ROOT_CAUSE_TEMPLATE)
        .put(SubscriptionGroupDTO.class, SubEntityType.SUBSCRIPTION_GROUP)
        .put(TaskDTO.class, SubEntityType.TASK)
        .build();
  }

  static String getType(final Class<? extends AbstractDTO> pojoClass) {
    final SubEntityType subEntityType = BEAN_TYPE_MAP.get(pojoClass);
    return requireNonNull(subEntityType, "entity type not found!").toString();
  }
}
