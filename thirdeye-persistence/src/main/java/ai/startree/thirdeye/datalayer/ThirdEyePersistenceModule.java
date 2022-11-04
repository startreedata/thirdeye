/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.datalayer;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import ai.startree.thirdeye.datalayer.bao.AlertManagerImpl;
import ai.startree.thirdeye.datalayer.bao.AlertSnapshotManagerImpl;
import ai.startree.thirdeye.datalayer.bao.AlertTemplateManagerImpl;
import ai.startree.thirdeye.datalayer.bao.AnomalyFunctionManagerImpl;
import ai.startree.thirdeye.datalayer.bao.AnomalySubscriptionGroupNotificationManagerImpl;
import ai.startree.thirdeye.datalayer.bao.DataSourceManagerImpl;
import ai.startree.thirdeye.datalayer.bao.DatasetConfigManagerImpl;
import ai.startree.thirdeye.datalayer.bao.DetectionStatusManagerImpl;
import ai.startree.thirdeye.datalayer.bao.EntityToEntityMappingManagerImpl;
import ai.startree.thirdeye.datalayer.bao.EnumerationItemManagerImpl;
import ai.startree.thirdeye.datalayer.bao.EventManagerImpl;
import ai.startree.thirdeye.datalayer.bao.GroupedAnomalyResultsManagerImpl;
import ai.startree.thirdeye.datalayer.bao.JobManagerImpl;
import ai.startree.thirdeye.datalayer.bao.MergedAnomalyResultManagerImpl;
import ai.startree.thirdeye.datalayer.bao.MetricConfigManagerImpl;
import ai.startree.thirdeye.datalayer.bao.OnboardDatasetMetricManagerImpl;
import ai.startree.thirdeye.datalayer.bao.OnlineDetectionDataManagerImpl;
import ai.startree.thirdeye.datalayer.bao.OverrideConfigManagerImpl;
import ai.startree.thirdeye.datalayer.bao.RcaInvestigationManagerImpl;
import ai.startree.thirdeye.datalayer.bao.RootcauseTemplateManagerImpl;
import ai.startree.thirdeye.datalayer.bao.SubscriptionGroupManagerImpl;
import ai.startree.thirdeye.datalayer.bao.TaskManagerImpl;
import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import ai.startree.thirdeye.datalayer.entity.AlertTemplateIndex;
import ai.startree.thirdeye.datalayer.entity.AnomalyFeedbackIndex;
import ai.startree.thirdeye.datalayer.entity.AnomalySubscriptionGroupNotificationIndex;
import ai.startree.thirdeye.datalayer.entity.DataSourceIndex;
import ai.startree.thirdeye.datalayer.entity.DatasetConfigIndex;
import ai.startree.thirdeye.datalayer.entity.DetectionAlertConfigIndex;
import ai.startree.thirdeye.datalayer.entity.DetectionConfigIndex;
import ai.startree.thirdeye.datalayer.entity.DetectionStatusIndex;
import ai.startree.thirdeye.datalayer.entity.EntityToEntityMappingIndex;
import ai.startree.thirdeye.datalayer.entity.EnumerationItemIndex;
import ai.startree.thirdeye.datalayer.entity.EventIndex;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.datalayer.entity.JobIndex;
import ai.startree.thirdeye.datalayer.entity.MergedAnomalyResultIndex;
import ai.startree.thirdeye.datalayer.entity.MetricConfigIndex;
import ai.startree.thirdeye.datalayer.entity.OnboardDatasetMetricIndex;
import ai.startree.thirdeye.datalayer.entity.OnlineDetectionDataIndex;
import ai.startree.thirdeye.datalayer.entity.OverrideConfigIndex;
import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.datalayer.entity.RootcauseTemplateIndex;
import ai.startree.thirdeye.datalayer.entity.TaskEntity;
import ai.startree.thirdeye.datalayer.entity.TaskIndex;
import ai.startree.thirdeye.datalayer.util.EntityMappingHolder;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertSnapshotManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyFunctionManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.DetectionStatusManager;
import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.GroupedAnomalyResultsManager;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.OnboardDatasetMetricManager;
import ai.startree.thirdeye.spi.datalayer.bao.OnlineDetectionDataManager;
import ai.startree.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseTemplateManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyePersistenceModule extends AbstractModule {

  private static final List<Class<? extends AbstractEntity>> ENTITY_CLASSES = Arrays.asList(
      // Main data table
      GenericJsonEntity.class,

      // All index tables
      AlertTemplateIndex.class,
      AnomalyFeedbackIndex.class,
      AnomalySubscriptionGroupNotificationIndex.class,
      DataSourceIndex.class,
      DatasetConfigIndex.class,
      DetectionAlertConfigIndex.class,
      DetectionConfigIndex.class,
      DetectionStatusIndex.class,
      EntityToEntityMappingIndex.class,
      EnumerationItemIndex.class,
      EventIndex.class,
      JobIndex.class,
      MergedAnomalyResultIndex.class,
      MetricConfigIndex.class,
      OnboardDatasetMetricIndex.class,
      OnlineDetectionDataIndex.class,
      OverrideConfigIndex.class,
      RcaInvestigationIndex.class,
      RootcauseTemplateIndex.class,
      TaskIndex.class,
      TaskEntity.class
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
    bind(EnumerationItemManager.class).to(EnumerationItemManagerImpl.class).in(Scopes.SINGLETON);
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
    bind(AlertSnapshotManager.class).to(AlertSnapshotManagerImpl.class).in(Scopes.SINGLETON);
    bind(RcaInvestigationManager.class).to(RcaInvestigationManagerImpl.class).in(Scopes.SINGLETON);
    bind(RootcauseTemplateManager.class).to(RootcauseTemplateManagerImpl.class)
        .in(Scopes.SINGLETON);
    bind(AlertManager.class).to(AlertManagerImpl.class).in(Scopes.SINGLETON);
    bind(AlertTemplateManager.class).to(AlertTemplateManagerImpl.class).in(Scopes.SINGLETON);
    bind(SubscriptionGroupManager.class).to(SubscriptionGroupManagerImpl.class).in(
        Scopes.SINGLETON);
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
