package org.apache.pinot.thirdeye.datalayer.dao;

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
import org.apache.pinot.thirdeye.datalayer.entity.TaskIndex;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.AnomalyFeedbackBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.AnomalySubscriptionGroupNotificationBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.ApplicationBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.DataSourceBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.DatasetConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.DetectionAlertConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.DetectionConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.DetectionStatusBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.EntityToEntityMappingBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.EvaluationBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.EventBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.JobBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.MergedAnomalyResultBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.MetricConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.OnboardDatasetMetricBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.OnlineDetectionDataBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.OverrideConfigBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.RootcauseSessionBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.RootcauseTemplateBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.SessionBean;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.TaskBean;

/**
 * ThirdEye entities consists of GenericJsonEntity and the index tables of the sub entities.
 * Sub Entities are the main business objects which are alerts, metrics, datasets, datasources, etc
 */
public class SubEntities {

  static final ImmutableMap<Class<? extends AbstractDTO>, Class<? extends AbstractIndexEntity>>
      beanIndexMap = buildBeanIndexMap();

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
        .put(ApplicationBean.class, ApplicationIndex.class)
        .put(RootcauseSessionBean.class, RootcauseSessionIndex.class)
        .put(SessionBean.class, SessionIndex.class)
        .put(DetectionConfigBean.class, DetectionConfigIndex.class)
        .put(DetectionAlertConfigBean.class, DetectionAlertConfigIndex.class)
        .put(EvaluationBean.class, EvaluationIndex.class)
        .put(RootcauseTemplateBean.class, RootcauseTemplateIndex.class)
        .put(OnlineDetectionDataBean.class, OnlineDetectionDataIndex.class)
        .put(AnomalySubscriptionGroupNotificationBean.class,
            AnomalySubscriptionGroupNotificationIndex.class)
        .build();
  }
}
