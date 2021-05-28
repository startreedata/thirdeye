package org.apache.pinot.thirdeye.datalayer.dao;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
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

public class EntityInfoBuilder {

  private static final String DEFAULT_BASE_TABLE_NAME = "GENERIC_JSON_ENTITY";
  private final Map<Class<? extends AbstractDTO>, EntityInfo> entityInfoMap;

  public EntityInfoBuilder() {
    entityInfoMap = buildEntityInfoMap();
  }

  private static EntityInfo entityInfo(
      final Class<? extends AbstractIndexEntity> indexEntityClass) {
    return new EntityInfo(DEFAULT_BASE_TABLE_NAME, indexEntityClass);
  }

  public Map<Class<? extends AbstractDTO>, EntityInfo> getEntityInfoMap() {
    return entityInfoMap;
  }

  private ImmutableMap<Class<? extends AbstractDTO>, EntityInfo> buildEntityInfoMap() {
    return ImmutableMap.<Class<? extends AbstractDTO>, EntityInfo>builder()
        .put(AnomalyFeedbackBean.class, entityInfo(AnomalyFeedbackIndex.class))
        .put(JobBean.class, entityInfo(JobIndex.class))
        .put(TaskBean.class, entityInfo(TaskIndex.class))
        .put(MergedAnomalyResultBean.class, entityInfo(MergedAnomalyResultIndex.class))
        .put(DataSourceBean.class, entityInfo(DataSourceIndex.class))
        .put(DatasetConfigBean.class, entityInfo(DatasetConfigIndex.class))
        .put(MetricConfigBean.class, entityInfo(MetricConfigIndex.class))
        .put(OverrideConfigBean.class, entityInfo(OverrideConfigIndex.class))
        .put(EventBean.class, entityInfo(EventIndex.class))
        .put(DetectionStatusBean.class, entityInfo(DetectionStatusIndex.class))
        .put(EntityToEntityMappingBean.class, entityInfo(EntityToEntityMappingIndex.class))
        .put(OnboardDatasetMetricBean.class, entityInfo(OnboardDatasetMetricIndex.class))
        .put(ApplicationBean.class, entityInfo(ApplicationIndex.class))
        .put(RootcauseSessionBean.class, entityInfo(RootcauseSessionIndex.class))
        .put(SessionBean.class, entityInfo(SessionIndex.class))
        .put(DetectionConfigBean.class, entityInfo(DetectionConfigIndex.class))
        .put(DetectionAlertConfigBean.class, entityInfo(DetectionAlertConfigIndex.class))
        .put(EvaluationBean.class, entityInfo(EvaluationIndex.class))
        .put(RootcauseTemplateBean.class, entityInfo(RootcauseTemplateIndex.class))
        .put(OnlineDetectionDataBean.class, entityInfo(OnlineDetectionDataIndex.class))
        .put(AnomalySubscriptionGroupNotificationBean.class,
            entityInfo(AnomalySubscriptionGroupNotificationIndex.class))
        .build();
  }

  static class EntityInfo {

    final String baseTableName;
    final Class<? extends AbstractIndexEntity> indexEntityClass;

    public EntityInfo(final String baseTableName,
        final Class<? extends AbstractIndexEntity> indexEntityClass) {
      this.baseTableName = baseTableName;
      this.indexEntityClass = indexEntityClass;
    }
  }
}
