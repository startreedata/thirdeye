package org.apache.pinot.thirdeye.datalayer.dao;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ALERT;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ALERT_TEMPLATE;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ANOMALY;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ANOMALY_FEEDBACK;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ANOMALY_SUBSCRIPTION_GROUP_NOTIFICATION;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.APPLICATION;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.DATASET;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.DATA_SOURCE;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.DETECTION_STATUS;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ENTITY_TO_ENTITY_MAPPING;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.EVALUATION;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.EVENT;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.JOB;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.METRIC;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ONBOARD_DATASET_METRIC;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ONLINE_DETECTION_DATA;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.OVERRIDE_CONFIG;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ROOT_CAUSE_SESSION;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ROOT_CAUSE_TEMPLATE;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.SUBSCRIPTION_GROUP;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.TASK;

import com.google.common.collect.ImmutableMap;
import org.apache.pinot.thirdeye.datalayer.entity.AbstractIndexEntity;
import org.apache.pinot.thirdeye.datalayer.entity.AlertTemplateIndex;
import org.apache.pinot.thirdeye.datalayer.entity.AnomalyFeedbackIndex;
import org.apache.pinot.thirdeye.datalayer.entity.AnomalySubscriptionGroupNotificationIndex;
import org.apache.pinot.thirdeye.datalayer.entity.ApplicationIndex;
import org.apache.pinot.thirdeye.datalayer.entity.DataSourceIndex;
import org.apache.pinot.thirdeye.datalayer.entity.DatasetConfigIndex;
import org.apache.pinot.thirdeye.datalayer.entity.DetectionAlertConfigIndex;
import org.apache.pinot.thirdeye.datalayer.entity.DetectionConfigIndex;
import org.apache.pinot.thirdeye.datalayer.entity.DetectionStatusIndex;
import org.apache.pinot.thirdeye.datalayer.entity.EntityToEntityMappingIndex;
import org.apache.pinot.thirdeye.datalayer.entity.EvaluationIndex;
import org.apache.pinot.thirdeye.datalayer.entity.EventIndex;
import org.apache.pinot.thirdeye.datalayer.entity.JobIndex;
import org.apache.pinot.thirdeye.datalayer.entity.MergedAnomalyResultIndex;
import org.apache.pinot.thirdeye.datalayer.entity.MetricConfigIndex;
import org.apache.pinot.thirdeye.datalayer.entity.OnboardDatasetMetricIndex;
import org.apache.pinot.thirdeye.datalayer.entity.OnlineDetectionDataIndex;
import org.apache.pinot.thirdeye.datalayer.entity.OverrideConfigIndex;
import org.apache.pinot.thirdeye.datalayer.entity.RootcauseSessionIndex;
import org.apache.pinot.thirdeye.datalayer.entity.RootcauseTemplateIndex;
import org.apache.pinot.thirdeye.datalayer.entity.SubEntityType;
import org.apache.pinot.thirdeye.datalayer.entity.TaskIndex;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EventDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.JobDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OnboardDatasetMetricDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OnlineDetectionDataDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RootcauseSessionDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RootcauseTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;

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
        .put(ApplicationDTO.class, ApplicationIndex.class)
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
        .put(RootcauseSessionDTO.class, RootcauseSessionIndex.class)
        .put(RootcauseTemplateDTO.class, RootcauseTemplateIndex.class)
        .put(SubscriptionGroupDTO.class, DetectionAlertConfigIndex.class)
        .put(TaskDTO.class, TaskIndex.class)
        .build();
  }

  private static ImmutableMap<Class<? extends AbstractDTO>, SubEntityType> buildTypeBeanBimap() {
    return ImmutableMap.<Class<? extends AbstractDTO>, SubEntityType>builder()
        .put(AlertDTO.class, ALERT)
        .put(AlertTemplateDTO.class, ALERT_TEMPLATE)
        .put(AnomalyFeedbackDTO.class, ANOMALY_FEEDBACK)
        .put(AnomalySubscriptionGroupNotificationDTO.class,
            ANOMALY_SUBSCRIPTION_GROUP_NOTIFICATION)
        .put(ApplicationDTO.class, APPLICATION)
        .put(DataSourceDTO.class, DATA_SOURCE)
        .put(DatasetConfigDTO.class, DATASET)
        .put(DetectionStatusDTO.class, DETECTION_STATUS)
        .put(EntityToEntityMappingDTO.class, ENTITY_TO_ENTITY_MAPPING)
        .put(EvaluationDTO.class, EVALUATION)
        .put(EventDTO.class, EVENT)
        .put(JobDTO.class, JOB)
        .put(MergedAnomalyResultDTO.class, ANOMALY)
        .put(MetricConfigDTO.class, METRIC)
        .put(OnboardDatasetMetricDTO.class, ONBOARD_DATASET_METRIC)
        .put(OnlineDetectionDataDTO.class, ONLINE_DETECTION_DATA)
        .put(OverrideConfigDTO.class, OVERRIDE_CONFIG)
        .put(RootcauseSessionDTO.class, ROOT_CAUSE_SESSION)
        .put(RootcauseTemplateDTO.class, ROOT_CAUSE_TEMPLATE)
        .put(SubscriptionGroupDTO.class, SUBSCRIPTION_GROUP)
        .put(TaskDTO.class, TASK)
        .build();
  }

  static String getType(final Class<? extends AbstractDTO> pojoClass) {
    final SubEntityType subEntityType = BEAN_TYPE_MAP.get(pojoClass);
    return requireNonNull(subEntityType, "entity type not found!").toString();
  }
}
