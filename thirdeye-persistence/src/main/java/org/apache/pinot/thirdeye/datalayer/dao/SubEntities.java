package org.apache.pinot.thirdeye.datalayer.dao;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.ALERT;
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
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.SESSION;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.SUBSCRIPTION_GROUP;
import static org.apache.pinot.thirdeye.datalayer.entity.SubEntityType.TASK;

import com.google.common.collect.ImmutableMap;
import org.apache.pinot.thirdeye.datalayer.entity.AbstractIndexEntity;
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
import org.apache.pinot.thirdeye.datalayer.entity.SessionIndex;
import org.apache.pinot.thirdeye.datalayer.entity.SubEntityType;
import org.apache.pinot.thirdeye.datalayer.entity.TaskIndex;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalyFeedbackBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DetectionStatusBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EntityToEntityMappingBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EvaluationBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EventBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.JobBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OnboardDatasetMetricBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OnlineDetectionDataBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OverrideConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RootcauseSessionBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RootcauseTemplateBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SessionBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskBean;

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
        .put(AnomalyFeedbackBean.class, AnomalyFeedbackIndex.class)
        .put(JobBean.class, JobIndex.class)
        .put(TaskBean.class, TaskIndex.class)
        .put(MergedAnomalyResultBean.class, MergedAnomalyResultIndex.class)
        .put(DataSourceBean.class, DataSourceIndex.class)
        .put(DatasetConfigBean.class, DatasetConfigIndex.class)
        .put(MetricConfigBean.class, MetricConfigIndex.class)
        .put(OverrideConfigBean.class, OverrideConfigIndex.class)
        .put(EventBean.class, EventIndex.class)
        .put(DetectionStatusBean.class, DetectionStatusIndex.class)
        .put(EntityToEntityMappingBean.class, EntityToEntityMappingIndex.class)
        .put(OnboardDatasetMetricBean.class, OnboardDatasetMetricIndex.class)
        .put(ApplicationDTO.class, ApplicationIndex.class)
        .put(RootcauseSessionBean.class, RootcauseSessionIndex.class)
        .put(SessionBean.class, SessionIndex.class)
        .put(AlertDTO.class, DetectionConfigIndex.class)
        .put(SubscriptionGroupDTO.class, DetectionAlertConfigIndex.class)
        .put(EvaluationBean.class, EvaluationIndex.class)
        .put(RootcauseTemplateBean.class, RootcauseTemplateIndex.class)
        .put(OnlineDetectionDataBean.class, OnlineDetectionDataIndex.class)
        .put(AnomalySubscriptionGroupNotificationBean.class,
            AnomalySubscriptionGroupNotificationIndex.class)
        .build();
  }

  private static ImmutableMap<Class<? extends AbstractDTO>, SubEntityType> buildTypeBeanBimap() {
    return ImmutableMap.<Class<? extends AbstractDTO>, SubEntityType>builder()
        .put(AnomalyFeedbackBean.class, ANOMALY_FEEDBACK)
        .put(JobBean.class, JOB)
        .put(TaskBean.class, TASK)
        .put(MergedAnomalyResultBean.class, ANOMALY)
        .put(DataSourceBean.class, DATA_SOURCE)
        .put(DatasetConfigBean.class, DATASET)
        .put(MetricConfigBean.class, METRIC)
        .put(OverrideConfigBean.class, OVERRIDE_CONFIG)
        .put(EventBean.class, EVENT)
        .put(DetectionStatusBean.class, DETECTION_STATUS)
        .put(EntityToEntityMappingBean.class, ENTITY_TO_ENTITY_MAPPING)
        .put(OnboardDatasetMetricBean.class, ONBOARD_DATASET_METRIC)
        .put(ApplicationDTO.class, APPLICATION)
        .put(RootcauseSessionBean.class, ROOT_CAUSE_SESSION)
        .put(SessionBean.class, SESSION)
        .put(AlertDTO.class, ALERT)
        .put(SubscriptionGroupDTO.class, SUBSCRIPTION_GROUP)
        .put(EvaluationBean.class, EVALUATION)
        .put(RootcauseTemplateBean.class, ROOT_CAUSE_TEMPLATE)
        .put(OnlineDetectionDataBean.class, ONLINE_DETECTION_DATA)
        .put(AnomalySubscriptionGroupNotificationBean.class,
            ANOMALY_SUBSCRIPTION_GROUP_NOTIFICATION)
        .build();
  }

  static String getType(final Class<? extends AbstractDTO> pojoClass) {
    final SubEntityType subEntityType = BEAN_TYPE_MAP.get(pojoClass);
    return requireNonNull(subEntityType, "entity type not found!").toString();
  }
}
