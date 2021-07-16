package org.apache.pinot.thirdeye.datalayer;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.AlertSnapshotManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.AlertTemplateManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.AnomalyFunctionManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.AnomalySubscriptionGroupNotificationManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.ApplicationManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.DataSourceManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.DetectionStatusManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.EntityToEntityMappingManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.EvaluationManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.EventManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.GroupedAnomalyResultsManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.JobManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.OnboardDatasetMetricManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.OnlineDetectionDataManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.OverrideConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.RootcauseSessionManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.RootcauseTemplateManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.SubscriptionGroupManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.TaskManagerImpl;
import org.apache.pinot.thirdeye.datalayer.entity.AbstractEntity;
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
import org.apache.pinot.thirdeye.datalayer.entity.GenericJsonEntity;
import org.apache.pinot.thirdeye.datalayer.entity.JobIndex;
import org.apache.pinot.thirdeye.datalayer.entity.MergedAnomalyResultIndex;
import org.apache.pinot.thirdeye.datalayer.entity.MetricConfigIndex;
import org.apache.pinot.thirdeye.datalayer.entity.OnboardDatasetMetricIndex;
import org.apache.pinot.thirdeye.datalayer.entity.OnlineDetectionDataIndex;
import org.apache.pinot.thirdeye.datalayer.entity.OverrideConfigIndex;
import org.apache.pinot.thirdeye.datalayer.entity.RootcauseSessionIndex;
import org.apache.pinot.thirdeye.datalayer.entity.RootcauseTemplateIndex;
import org.apache.pinot.thirdeye.datalayer.entity.TaskIndex;
import org.apache.pinot.thirdeye.datalayer.util.EntityMappingHolder;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertSnapshotManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalyFunctionManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DataSourceManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DetectionStatusManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.GroupedAnomalyResultsManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.JobManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.OnboardDatasetMetricManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.OnlineDetectionDataManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.RootcauseSessionManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.RootcauseTemplateManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyePersistenceModule extends AbstractModule {

  private static final List<Class<? extends AbstractEntity>> ENTITY_CLASSES = Arrays.asList(
      // Main data table
      GenericJsonEntity.class,

      // All index tables
      AlertTemplateIndex.class,
      AnomalyFeedbackIndex.class,
      AnomalySubscriptionGroupNotificationIndex.class,
      ApplicationIndex.class,
      DataSourceIndex.class,
      DatasetConfigIndex.class,
      DetectionAlertConfigIndex.class,
      DetectionConfigIndex.class,
      DetectionStatusIndex.class,
      EntityToEntityMappingIndex.class,
      EvaluationIndex.class,
      EventIndex.class,
      JobIndex.class,
      MergedAnomalyResultIndex.class,
      MetricConfigIndex.class,
      OnboardDatasetMetricIndex.class,
      OnlineDetectionDataIndex.class,
      OverrideConfigIndex.class,
      RootcauseSessionIndex.class,
      RootcauseTemplateIndex.class,
      TaskIndex.class
  );

  private final DataSource dataSource;

  public ThirdEyePersistenceModule(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static String camelCaseToUnderscore(String str) {
    return UPPER_CAMEL.to(LOWER_UNDERSCORE, str);
  }

  @Override
  protected void configure() {
    bind(javax.sql.DataSource.class).toInstance(dataSource);
    bind(DataSource.class).toInstance(dataSource);

    bind(AnomalyFunctionManager.class).to(AnomalyFunctionManagerImpl.class).in(Scopes.SINGLETON);
    bind(MergedAnomalyResultManager.class).to(MergedAnomalyResultManagerImpl.class).in(
        Scopes.SINGLETON);
    bind(JobManager.class).to(JobManagerImpl.class).in(Scopes.SINGLETON);
    bind(TaskManager.class).to(TaskManagerImpl.class).in(Scopes.SINGLETON);
    bind(DataSourceManager.class).to(DataSourceManagerImpl.class).in(Scopes.SINGLETON);
    bind(DatasetConfigManager.class).to(DatasetConfigManagerImpl.class).in(Scopes.SINGLETON);
    bind(MetricConfigManager.class).to(MetricConfigManagerImpl.class).in(Scopes.SINGLETON);
    bind(OverrideConfigManager.class).to(OverrideConfigManagerImpl.class).in(Scopes.SINGLETON);
    bind(EventManager.class).to(EventManagerImpl.class).in(Scopes.SINGLETON);
    bind(DetectionStatusManager.class).to(DetectionStatusManagerImpl.class).in(Scopes.SINGLETON);
    bind(EntityToEntityMappingManager.class).to(EntityToEntityMappingManagerImpl.class)
        .in(Scopes.SINGLETON);
    bind(GroupedAnomalyResultsManager.class).to(GroupedAnomalyResultsManagerImpl.class)
        .in(Scopes.SINGLETON);
    bind(OnboardDatasetMetricManager.class).to(OnboardDatasetMetricManagerImpl.class).in(
        Scopes.SINGLETON);
    bind(ApplicationManager.class).to(ApplicationManagerImpl.class).in(Scopes.SINGLETON);
    bind(AlertSnapshotManager.class).to(AlertSnapshotManagerImpl.class).in(Scopes.SINGLETON);
    bind(RootcauseSessionManager.class).to(RootcauseSessionManagerImpl.class).in(Scopes.SINGLETON);
    bind(RootcauseTemplateManager.class).to(RootcauseTemplateManagerImpl.class)
        .in(Scopes.SINGLETON);
    bind(AlertManager.class).to(AlertManagerImpl.class).in(Scopes.SINGLETON);
    bind(AlertTemplateManager.class).to(AlertTemplateManagerImpl.class).in(Scopes.SINGLETON);
    bind(SubscriptionGroupManager.class).to(SubscriptionGroupManagerImpl.class).in(
        Scopes.SINGLETON);
    bind(EvaluationManager.class).to(EvaluationManagerImpl.class).in(Scopes.SINGLETON);
    bind(OnlineDetectionDataManager.class).to(OnlineDetectionDataManagerImpl.class).in(
        Scopes.SINGLETON);
    bind(AnomalySubscriptionGroupNotificationManager.class)
        .to(AnomalySubscriptionGroupNotificationManagerImpl.class)
        .in(Scopes.SINGLETON);
  }

  @Singleton
  @Provides
  public EntityMappingHolder getEntityMappingHolder(final DataSource dataSource) {
    final EntityMappingHolder entityMappingHolder = new EntityMappingHolder();
    try (Connection connection = dataSource.getConnection()) {
      for (Class<? extends AbstractEntity> clazz : ENTITY_CLASSES) {
        final String tableName = camelCaseToUnderscore(clazz.getSimpleName());
        entityMappingHolder.register(connection, clazz, tableName);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return entityMappingHolder;
  }
}
