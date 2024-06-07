/*
 * Copyright 2024 StarTree Inc
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
import ai.startree.thirdeye.datalayer.bao.AlertTemplateManagerImpl;
import ai.startree.thirdeye.datalayer.bao.AnomalyManagerImpl;
import ai.startree.thirdeye.datalayer.bao.AnomalySubscriptionGroupNotificationManagerImpl;
import ai.startree.thirdeye.datalayer.bao.DataSourceManagerImpl;
import ai.startree.thirdeye.datalayer.bao.DatasetConfigManagerImpl;
import ai.startree.thirdeye.datalayer.bao.EnumerationItemManagerImpl;
import ai.startree.thirdeye.datalayer.bao.EventManagerImpl;
import ai.startree.thirdeye.datalayer.bao.MetricConfigManagerImpl;
import ai.startree.thirdeye.datalayer.bao.RcaInvestigationManagerImpl;
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
import ai.startree.thirdeye.datalayer.entity.EnumerationItemIndex;
import ai.startree.thirdeye.datalayer.entity.EventIndex;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.datalayer.entity.MergedAnomalyResultIndex;
import ai.startree.thirdeye.datalayer.entity.MetricConfigIndex;
import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.datalayer.entity.TaskEntity;
import ai.startree.thirdeye.datalayer.util.EntityMappingHolder;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
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
      EnumerationItemIndex.class,
      EventIndex.class,
      MergedAnomalyResultIndex.class,
      MetricConfigIndex.class,
      RcaInvestigationIndex.class,
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

    bind(AnomalyManager.class).to(AnomalyManagerImpl.class).in(
        Scopes.SINGLETON);
    bind(TaskManager.class).to(TaskManagerImpl.class).in(Scopes.SINGLETON);
    bind(DataSourceManager.class).to(DataSourceManagerImpl.class).in(Scopes.SINGLETON);
    bind(DatasetConfigManager.class).to(DatasetConfigManagerImpl.class).in(Scopes.SINGLETON);
    bind(EnumerationItemManager.class).to(EnumerationItemManagerImpl.class).in(Scopes.SINGLETON);
    bind(MetricConfigManager.class).to(MetricConfigManagerImpl.class).in(Scopes.SINGLETON);
    bind(EventManager.class).to(EventManagerImpl.class).in(Scopes.SINGLETON);
    bind(RcaInvestigationManager.class).to(RcaInvestigationManagerImpl.class).in(Scopes.SINGLETON);
    bind(AlertManager.class).to(AlertManagerImpl.class).in(Scopes.SINGLETON);
    bind(AlertTemplateManager.class).to(AlertTemplateManagerImpl.class).in(Scopes.SINGLETON);
    bind(SubscriptionGroupManager.class).to(SubscriptionGroupManagerImpl.class).in(
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
