/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datasource;

import org.apache.pinot.thirdeye.datalayer.bao.AlertConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.AlertSnapshotManager;
import org.apache.pinot.thirdeye.datalayer.bao.AnomalyFunctionManager;
import org.apache.pinot.thirdeye.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.datalayer.bao.ClassificationConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.ConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.DataCompletenessConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.DetectionStatusManager;
import org.apache.pinot.thirdeye.datalayer.bao.EntityToEntityMappingManager;
import org.apache.pinot.thirdeye.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.datalayer.bao.GroupedAnomalyResultsManager;
import org.apache.pinot.thirdeye.datalayer.bao.JobManager;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.OnboardDatasetMetricManager;
import org.apache.pinot.thirdeye.datalayer.bao.OnlineDetectionDataManager;
import org.apache.pinot.thirdeye.datalayer.bao.OverrideConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.RawAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.RootcauseSessionManager;
import org.apache.pinot.thirdeye.datalayer.bao.RootcauseTemplateManager;
import org.apache.pinot.thirdeye.datalayer.bao.SessionManager;
import org.apache.pinot.thirdeye.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.AlertConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.AlertManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.AlertSnapshotManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.AnomalyFunctionManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.AnomalySubscriptionGroupNotificationManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.ApplicationManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.ClassificationConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.ConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.DataCompletenessConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.DatasetConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.DetectionStatusManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.EntityToEntityMappingManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.EvaluationManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.EventManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.GroupedAnomalyResultsManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.JobManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.MergedAnomalyResultManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.MetricConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.OnboardDatasetMetricManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.OnlineDetectionDataManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.OverrideConfigManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.RawAnomalyResultManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.RootcauseSessionManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.RootcauseTemplateManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.SessionManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.SubscriptionGroupManagerImpl;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.TaskManagerImpl;
import org.apache.pinot.thirdeye.util.DeprecatedInjectorUtil;

/**
 * Singleton service registry for Data Access Objects (DAOs)
 */
public class DAORegistry {

  private static final DAORegistry INSTANCE = new DAORegistry();

  /****************************************************************************
   * SINGLETON
   */

  public static DAORegistry getInstance() {
    return INSTANCE;
  }

  /**
   * internal constructor.
   */
  private DAORegistry() {
  }

  /****************************************************************************
   * GETTERS/SETTERS
   */

  public AnomalyFunctionManager getAnomalyFunctionDAO() {
    return DeprecatedInjectorUtil.getInstance(AnomalyFunctionManagerImpl.class);
  }

  public AlertConfigManager getAlertConfigDAO() {
    return DeprecatedInjectorUtil.getInstance(AlertConfigManagerImpl.class);
  }

  public RawAnomalyResultManager getRawAnomalyResultDAO() {
    return DeprecatedInjectorUtil.getInstance(RawAnomalyResultManagerImpl.class);
  }

  public MergedAnomalyResultManager getMergedAnomalyResultDAO() {
    return DeprecatedInjectorUtil.getInstance(MergedAnomalyResultManagerImpl.class);
  }

  public JobManager getJobDAO() {
    return DeprecatedInjectorUtil.getInstance(JobManagerImpl.class);
  }

  public TaskManager getTaskDAO() {
    return DeprecatedInjectorUtil.getInstance(TaskManagerImpl.class);
  }

  public DatasetConfigManager getDatasetConfigDAO() {
    return DeprecatedInjectorUtil.getInstance(DatasetConfigManagerImpl.class);
  }

  public MetricConfigManager getMetricConfigDAO() {
    return DeprecatedInjectorUtil.getInstance(MetricConfigManagerImpl.class);
  }

  public OverrideConfigManager getOverrideConfigDAO() {
    return DeprecatedInjectorUtil.getInstance(OverrideConfigManagerImpl.class);
  }

  public DataCompletenessConfigManager getDataCompletenessConfigDAO() {
    return DeprecatedInjectorUtil.getInstance(DataCompletenessConfigManagerImpl.class);
  }

  public EventManager getEventDAO() {
    return DeprecatedInjectorUtil.getInstance(EventManagerImpl.class);
  }

  public DetectionStatusManager getDetectionStatusDAO() {
    return DeprecatedInjectorUtil.getInstance(DetectionStatusManagerImpl.class);
  }

  public ClassificationConfigManager getClassificationConfigDAO() {
    return DeprecatedInjectorUtil.getInstance(ClassificationConfigManagerImpl.class);
  }

  public EntityToEntityMappingManager getEntityToEntityMappingDAO() {
    return DeprecatedInjectorUtil.getInstance(EntityToEntityMappingManagerImpl.class);
  }

  public GroupedAnomalyResultsManager getGroupedAnomalyResultsDAO() {
    return DeprecatedInjectorUtil.getInstance(GroupedAnomalyResultsManagerImpl.class);
  }

  public OnboardDatasetMetricManager getOnboardDatasetMetricDAO() {
    return DeprecatedInjectorUtil.getInstance(OnboardDatasetMetricManagerImpl.class);
  }

  public ConfigManager getConfigDAO() {
    return DeprecatedInjectorUtil.getInstance(ConfigManagerImpl.class);
  }

  public ApplicationManager getApplicationDAO() {
    return DeprecatedInjectorUtil.getInstance(ApplicationManagerImpl.class);
  }

  public AlertSnapshotManager getAlertSnapshotDAO() {
    return DeprecatedInjectorUtil.getInstance(AlertSnapshotManagerImpl.class);
  }

  public RootcauseSessionManager getRootcauseSessionDAO() {
    return DeprecatedInjectorUtil.getInstance(RootcauseSessionManagerImpl.class);
  }

  public RootcauseTemplateManager getRootcauseTemplateDao() {
    return DeprecatedInjectorUtil.getInstance(RootcauseTemplateManagerImpl.class);
  }

  public SessionManager getSessionDAO() {
    return DeprecatedInjectorUtil.getInstance(SessionManagerImpl.class);
  }

  public AlertManager getDetectionConfigManager() {
    return DeprecatedInjectorUtil.getInstance(AlertManagerImpl.class);
  }

  public SubscriptionGroupManager getDetectionAlertConfigManager() {
    return DeprecatedInjectorUtil.getInstance(SubscriptionGroupManagerImpl.class);
  }

  public EvaluationManager getEvaluationManager() {
    return DeprecatedInjectorUtil.getInstance(EvaluationManagerImpl.class);
  }

  public OnlineDetectionDataManager getOnlineDetectionDataManager() {
    return DeprecatedInjectorUtil.getInstance(OnlineDetectionDataManagerImpl.class);
  }

  public AnomalySubscriptionGroupNotificationManager getAnomalySubscriptionGroupNotificationManager() {
    return DeprecatedInjectorUtil
        .getInstance(AnomalySubscriptionGroupNotificationManagerImpl.class);
  }
}
