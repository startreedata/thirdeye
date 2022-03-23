/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

/**
 * Deprecated in favor of using injected {@link MetricRegistry}
 */
@Deprecated
public class ThirdeyeMetricsUtil {

  private static final MetricRegistry metricRegistry = new MetricRegistry();

  public static final Counter couchbaseCallCounter =
      metricRegistry.counter("couchbaseCallCounter");
  public static final Counter couchbaseWriteCounter =
      metricRegistry.counter("couchbaseWriteCounter");
  public static final Counter couchbaseExceptionCounter =
      metricRegistry.counter("couchbaseExceptionCounter");
  public static final Counter rcaPipelineCallCounter =
      metricRegistry.counter("rcaPipelineCallCounter");
  public static final Counter rcaPipelineDurationCounter =
      metricRegistry.counter("rcaPipelineDurationCounter");
  public static final Counter rcaPipelineExceptionCounter =
      metricRegistry.counter("rcaPipelineExceptionCounter");
  public static final Counter rcaFrameworkCallCounter =
      metricRegistry.counter("rcaFrameworkCallCounter");
  public static final Counter rcaFrameworkDurationCounter =
      metricRegistry.counter("rcaFrameworkDurationCounter");
  public static final Counter rcaFrameworkExceptionCounter =
      metricRegistry.counter("rcaFrameworkExceptionCounter");
  public static final Counter cubeCallCounter =
      metricRegistry.counter("cubeCallCounter");
  public static final Counter cubeDurationCounter =
      metricRegistry.counter("cubeDurationCounter");
  public static final Counter cubeExceptionCounter =
      metricRegistry.counter("cubeExceptionCounter");
  public static final Counter detectionRetuneCounter =
      metricRegistry.counter("detectionRetuneCounter");
  public static final Counter triggerEventCounter =
      metricRegistry.counter("triggerEventCounter");
  public static final Counter processedTriggerEventCounter =
      metricRegistry.counter("processedTriggerEventCounter");
  public static final Counter eventScheduledTaskCounter =
      metricRegistry.counter("eventScheduledTaskCounter");
  public static final Counter eventScheduledTaskFallbackCounter =
      metricRegistry.counter("eventScheduledTaskFallbackCounter");
  public static final Counter jiraAlertsSuccessCounter =
      metricRegistry.counter("jiraAlertsSuccessCounter");
  public static final Counter jiraAlertsFailedCounter =
      metricRegistry.counter("jiraAlertsFailedCounter");
  public static final Counter jiraAlertsNumTicketsCounter =
      metricRegistry.counter("jiraAlertsNumTicketsCounter");
  public static final Counter jiraAlertsNumCommentsCounter =
      metricRegistry.counter("jiraAlertsNumCommentsCounter");

  private ThirdeyeMetricsUtil() {
  }
}
