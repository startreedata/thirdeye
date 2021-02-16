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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.AlertSnapshotManager;
import org.apache.pinot.thirdeye.datalayer.bao.AnomalyFunctionManager;
import org.apache.pinot.thirdeye.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.datalayer.bao.ApplicationManager;
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
import org.apache.pinot.thirdeye.datalayer.bao.RootcauseSessionManager;
import org.apache.pinot.thirdeye.datalayer.bao.RootcauseTemplateManager;
import org.apache.pinot.thirdeye.datalayer.bao.SessionManager;
import org.apache.pinot.thirdeye.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.datalayer.bao.TaskManager;

@Singleton
public class DAORegistry {

  private final AnomalyFunctionManager anomalyFunctionManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final JobManager jobManager;
  private final TaskManager taskManager;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;
  private final OverrideConfigManager overrideConfigManager;
  private final EventManager eventManager;
  private final DetectionStatusManager detectionStatusManager;
  private final EntityToEntityMappingManager entityToEntityMappingManager;
  private final GroupedAnomalyResultsManager groupedAnomalyResultsManager;
  private final OnboardDatasetMetricManager onboardDatasetMetricManager;
  private final ApplicationManager applicationManager;
  private final AlertSnapshotManager alertSnapshotManager;
  private final RootcauseSessionManager rootcauseSessionManager;
  private final RootcauseTemplateManager rootcauseTemplateManager;
  private final SessionManager sessionManager;
  private final AlertManager alertManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final EvaluationManager evaluationManager;
  private final OnlineDetectionDataManager onlineDetectionDataManager;
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;

  @Inject
  public DAORegistry(
      final AnomalyFunctionManager anomalyFunctionManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final JobManager jobManager,
      final TaskManager taskManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager,
      final OverrideConfigManager overrideConfigManager,
      final EventManager eventManager,
      final DetectionStatusManager detectionStatusManager,
      final EntityToEntityMappingManager entityToEntityMappingManager,
      final GroupedAnomalyResultsManager groupedAnomalyResultsManager,
      final OnboardDatasetMetricManager onboardDatasetMetricManager,
      final ApplicationManager applicationManager,
      final AlertSnapshotManager alertSnapshotManager,
      final RootcauseSessionManager rootcauseSessionManager,
      final RootcauseTemplateManager rootcauseTemplateManager,
      final SessionManager sessionManager,
      final AlertManager alertManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final EvaluationManager evaluationManager,
      final OnlineDetectionDataManager onlineDetectionDataManager,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    this.anomalyFunctionManager = anomalyFunctionManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.jobManager = jobManager;
    this.taskManager = taskManager;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
    this.overrideConfigManager = overrideConfigManager;
    this.eventManager = eventManager;
    this.detectionStatusManager = detectionStatusManager;
    this.entityToEntityMappingManager = entityToEntityMappingManager;
    this.groupedAnomalyResultsManager = groupedAnomalyResultsManager;
    this.onboardDatasetMetricManager = onboardDatasetMetricManager;
    this.applicationManager = applicationManager;
    this.alertSnapshotManager = alertSnapshotManager;
    this.rootcauseSessionManager = rootcauseSessionManager;
    this.rootcauseTemplateManager = rootcauseTemplateManager;
    this.sessionManager = sessionManager;
    this.alertManager = alertManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.evaluationManager = evaluationManager;
    this.onlineDetectionDataManager = onlineDetectionDataManager;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
  }

  public AnomalyFunctionManager getAnomalyFunctionDAO() {
    return anomalyFunctionManager;
  }

  public MergedAnomalyResultManager getMergedAnomalyResultDAO() {
    return mergedAnomalyResultManager;
  }

  public JobManager getJobDAO() {
    return jobManager;
  }

  public TaskManager getTaskDAO() {
    return taskManager;
  }

  public DatasetConfigManager getDatasetConfigDAO() {
    return datasetConfigManager;
  }

  public MetricConfigManager getMetricConfigDAO() {
    return metricConfigManager;
  }

  public OverrideConfigManager getOverrideConfigDAO() {
    return overrideConfigManager;
  }

  public EventManager getEventDAO() {
    return eventManager;
  }

  public DetectionStatusManager getDetectionStatusDAO() {
    return detectionStatusManager;
  }

  public EntityToEntityMappingManager getEntityToEntityMappingDAO() {
    return entityToEntityMappingManager;
  }

  public GroupedAnomalyResultsManager getGroupedAnomalyResultsDAO() {
    return groupedAnomalyResultsManager;
  }

  public OnboardDatasetMetricManager getOnboardDatasetMetricDAO() {
    return onboardDatasetMetricManager;
  }

  public ApplicationManager getApplicationDAO() {
    return applicationManager;
  }

  public AlertSnapshotManager getAlertSnapshotDAO() {
    return alertSnapshotManager;
  }

  public RootcauseSessionManager getRootcauseSessionDAO() {
    return rootcauseSessionManager;
  }

  public RootcauseTemplateManager getRootcauseTemplateDao() {
    return rootcauseTemplateManager;
  }

  public SessionManager getSessionDAO() {
    return sessionManager;
  }

  public AlertManager getDetectionConfigManager() {
    return alertManager;
  }

  public SubscriptionGroupManager getDetectionAlertConfigManager() {
    return subscriptionGroupManager;
  }

  public EvaluationManager getEvaluationManager() {
    return evaluationManager;
  }

  public OnlineDetectionDataManager getOnlineDetectionDataManager() {
    return onlineDetectionDataManager;
  }

  public AnomalySubscriptionGroupNotificationManager getAnomalySubscriptionGroupNotificationManager() {
    return anomalySubscriptionGroupNotificationManager;
  }
}
