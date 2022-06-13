/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource;

import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EvaluationManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DAORegistry {

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final TaskManager taskManager;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;
  private final EventManager eventManager;
  private final AlertManager alertManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final EvaluationManager evaluationManager;
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;

  @Inject
  public DAORegistry(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final TaskManager taskManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager,
      final EventManager eventManager,
      final AlertManager alertManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final EvaluationManager evaluationManager,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.taskManager = taskManager;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
    this.eventManager = eventManager;
    this.alertManager = alertManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.evaluationManager = evaluationManager;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
  }

  public MergedAnomalyResultManager getMergedAnomalyResultDAO() {
    return mergedAnomalyResultManager;
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

  public EventManager getEventDAO() {
    return eventManager;
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

  public AnomalySubscriptionGroupNotificationManager getAnomalySubscriptionGroupNotificationManager() {
    return anomalySubscriptionGroupNotificationManager;
  }
}
