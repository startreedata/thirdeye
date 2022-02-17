/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.content;

import ai.startree.thirdeye.notification.NotificationContext;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import java.util.Collection;
import java.util.Map;

/**
 * Defines the notification content interface.
 */
public interface NotificationContent {

  String EVENT_FILTER_COUNTRY = "countryCode";
  /*  The Event Crawl Offset takes the standard period format, ex: P1D for 1 day, P1W for 1 week
    Y: years     M: months              W: weeks
    D: days      H: hours (after T)     M: minutes (after T)
    S: seconds along with milliseconds (after T) */
  String EVENT_CRAWL_OFFSET = "eventCrawlOffset";
  String PRE_EVENT_CRAWL_OFFSET = "preEventCrawlOffset";
  String POST_EVENT_CRAWL_OFFSET = "postEventCrawlOffset";
  String INCLUDE_SENT_ANOMALY_ONLY = "includeSentAnomaliesOnly";
  String INCLUDE_SUMMARY = "includeSummary";
  String TIME_ZONE = "timezone";
  String DEFAULT_INCLUDE_SENT_ANOMALY_ONLY = "false";
  String DEFAULT_INCLUDE_SUMMARY = "false";
  String DEFAULT_DATE_PATTERN = "MMM dd, yyyy HH:mm";
  String DEFAULT_TIME_ZONE = "America/Los_Angeles";
  String DEFAULT_EVENT_CRAWL_OFFSET = "P2D";
  String RAW_VALUE_FORMAT = "%.0f";
  String PERCENTAGE_FORMAT = "%.2f %%";

  /**
   * Initialize the content formatter
   */
  void init(NotificationContext context);

  /**
   * Generate the template dictionary from the list of anomaly results to render in the template
   */
  Map<String, Object> format(Collection<AnomalyResult> anomalies, SubscriptionGroupDTO subsConfig);

  /**
   * Retrieves the template file (.ftl)
   */
  String getTemplate();

  /**
   * Path to the img which contains the anomaly snapshot
   */
  String getSnaphotPath();

  /**
   * Cleanup any temporary data
   */
  void cleanup();
}
