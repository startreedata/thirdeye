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

package org.apache.pinot.thirdeye.notification.content.templates;

import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.detection.anomaly.alert.util.AlertScreenshotHelper;
import org.apache.pinot.thirdeye.notification.content.AnomalyReportEntity;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.restclient.ThirdEyeRcaRestClient;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EventDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFeedback;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.util.SpiUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This email formatter lists the anomalies by their functions or metric.
 */
@Singleton
public class MetricAnomaliesContent extends BaseNotificationContent {

  private static final Logger LOG = LoggerFactory.getLogger(MetricAnomaliesContent.class);

  private AlertManager configDAO = null;
  private ThirdEyeRcaRestClient rcaClient;

  @Inject
  public MetricAnomaliesContent(final MetricConfigManager metricConfigManager,
      final EventManager eventManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager) {
    super(metricConfigManager, eventManager, mergedAnomalyResultManager);
    this.configDAO = detectionConfigManager;
  }

  @Override
  public void init(Properties properties, ThirdEyeCoordinatorConfiguration configuration) {
    super.init(properties, configuration);

    if (this.rcaClient == null) {
      final ThirdEyePrincipal principal = new ThirdEyePrincipal(
          this.thirdEyeAnomalyConfig.getTeRestConfig().getAdminUser(),
          this.thirdEyeAnomalyConfig.getTeRestConfig().getSessionKey()
      );
      this.rcaClient = new ThirdEyeRcaRestClient(principal,
          this.thirdEyeAnomalyConfig.getUiConfiguration().getExternalUrl());
    }
  }

  @Override
  public String getTemplate() {
    return MetricAnomaliesContent.class.getSimpleName();
  }

  @Override
  public Map<String, Object> format(Collection<AnomalyResult> anomalies,
      SubscriptionGroupDTO subsConfig) {
    Map<String, Object> templateData = super.getTemplateData(subsConfig, anomalies);
    enrichMetricInfo(templateData, anomalies);

    DateTime windowStart = DateTime.now();
    DateTime windowEnd = new DateTime(0);

    Map<String, Long> functionToId = new HashMap<>();
    Multimap<String, String> anomalyDimensions = ArrayListMultimap.create();
    Multimap<String, AnomalyReportEntity> functionAnomalyReports = ArrayListMultimap.create();
    Multimap<String, AnomalyReportEntity> metricAnomalyReports = ArrayListMultimap.create();
    List<AnomalyReportEntity> anomalyDetails = new ArrayList<>();
    List<String> anomalyIds = new ArrayList<>();

    List<AnomalyResult> sortedAnomalies = new ArrayList<>(anomalies);
    sortedAnomalies.sort(Comparator.comparingDouble(AnomalyResult::getWeight));

    for (AnomalyResult anomalyResult : anomalies) {
      if (!(anomalyResult instanceof MergedAnomalyResultDTO)) {
        LOG.warn("Anomaly result {} isn't an instance of MergedAnomalyResultDTO. Skip from alert.",
            anomalyResult);
        continue;
      }
      MergedAnomalyResultDTO anomaly = (MergedAnomalyResultDTO) anomalyResult;

      DateTime anomalyStartTime = new DateTime(anomaly.getStartTime(), dateTimeZone);
      DateTime anomalyEndTime = new DateTime(anomaly.getEndTime(), dateTimeZone);

      if (anomalyStartTime.isBefore(windowStart)) {
        windowStart = anomalyStartTime;
      }
      if (anomalyEndTime.isAfter(windowEnd)) {
        windowEnd = anomalyEndTime;
      }

      AnomalyFeedback feedback = anomaly.getFeedback();

      String feedbackVal = getFeedbackValue(feedback);

      String functionName = "Alerts";
      String funcDescription = "";
      Long id = -1L;

      if (anomaly.getDetectionConfigId() != null) {
        AlertDTO config = this.configDAO.findById(anomaly.getDetectionConfigId());
        Preconditions.checkNotNull(config,
            String.format("Cannot find detection config %d", anomaly.getDetectionConfigId()));
        functionName = config.getName();
        funcDescription = config.getDescription() == null ? "" : config.getDescription();
        id = config.getId();
      }

      Properties props = new Properties();
      props.putAll(anomaly.getProperties());
      double lift = BaseNotificationContent
          .getLift(anomaly.getAvgCurrentVal(), anomaly.getAvgBaselineVal());
      AnomalyReportEntity anomalyReport = new AnomalyReportEntity(String.valueOf(anomaly.getId()),
          getAnomalyURL(anomaly, this.thirdEyeAnomalyConfig.getUiConfiguration().getExternalUrl()),
          getPredictedValue(anomaly),
          getCurrentValue(anomaly),
          getFormattedLiftValue(anomaly, lift),
          getLiftDirection(lift),
          0d,
          getDimensionsList(anomaly.getDimensionMap()),
          getTimeDiffInHours(anomaly.getStartTime(), anomaly.getEndTime()), // duration
          feedbackVal,
          functionName,
          funcDescription,
          anomaly.getMetric(),
          getDateString(anomaly.getStartTime(), dateTimeZone),
          getDateString(anomaly.getEndTime(), dateTimeZone),
          getTimezoneString(dateTimeZone),
          getIssueType(anomaly),
          anomaly.getType().getLabel(),
          SpiUtils.encodeCompactedProperties(props),
          anomaly.getMetricUrn()
      );

      // dimension filters / values
      for (Map.Entry<String, String> entry : anomaly.getDimensions().entrySet()) {
        anomalyDimensions.put(entry.getKey(), entry.getValue());
      }

      // include notified alerts only in the email
      if (!includeSentAnomaliesOnly || anomaly.isNotified()) {
        anomalyDetails.add(anomalyReport);
        anomalyIds.add(anomalyReport.getAnomalyId());
        functionAnomalyReports.put(functionName, anomalyReport);
        metricAnomalyReports.put(optional(anomaly.getMetric()).orElse("UNKNOWN"), anomalyReport);
        functionToId.put(functionName, id);
      }
    }

    // holidays
    final DateTime eventStart = windowStart.minus(preEventCrawlOffset);
    final DateTime eventEnd = windowEnd.plus(postEventCrawlOffset);
    Map<String, List<String>> targetDimensions = new HashMap<>();
    if (thirdEyeAnomalyConfig.getHolidayCountriesWhitelist() != null) {
      targetDimensions
          .put(EVENT_FILTER_COUNTRY, thirdEyeAnomalyConfig.getHolidayCountriesWhitelist());
    }
    List<EventDTO> holidays = getHolidayEvents(eventStart, eventEnd, targetDimensions);
    holidays.sort(Comparator.comparingLong(EventDTO::getStartTime));

    // Insert anomaly snapshot image
    if (anomalyDetails.size() == 1) {
      AnomalyReportEntity singleAnomaly = anomalyDetails.get(0);
      try {
        this.imgPath = AlertScreenshotHelper
            .takeGraphScreenShot(singleAnomaly.getAnomalyId(), thirdEyeAnomalyConfig);
      } catch (Exception e) {
        LOG.error("Exception while embedding screenshot for anomaly {}",
            singleAnomaly.getAnomalyId(), e);
      }
    }

    templateData.put("anomalyDetails", anomalyDetails);
    templateData.put("anomalyIds", Joiner.on(",").join(anomalyIds));
    templateData.put("holidays", holidays);
    templateData.put("detectionToAnomalyDetailsMap", functionAnomalyReports.asMap());
    templateData.put("metricToAnomalyDetailsMap", metricAnomalyReports.asMap());
    templateData.put("functionToId", functionToId);

    // Display RCA highlights in email only if report contains anomalies belonging to a single metric.
    // Note: Once we have a sophisticated rca highlight support and users start seeing value, we'll
    // enable it for all the metrics.
    //TODO: the API's for RCA had changed. Need to migrate
/*    if (this.rcaClient != null && metricAnomalyReports.keySet().size() == 1) {
      String anomalyId = metricAnomalyReports.values().iterator().next().getAnomalyId();
      try {
        Map<String, Object> rcaHighlights = this.rcaClient
            .getRootCauseHighlights(Long.parseLong(anomalyId));
        templateData.put("cubeDimensions",
            ConfigUtils.getMap(rcaHighlights.get("cubeResults")).get("dimensions"));
        templateData.put("cubeResponseRows",
            ConfigUtils.getMap(rcaHighlights.get("cubeResults")).get("responseRows"));
      } catch (Exception e) {
        // alert notification shouldn't fail if rca insights are not available
        LOG.error("Skip Embedding RCA in email. Failed to retrieve the RCA Highlights for anomaly "
            + anomalyId, e);
      }
    }*/

    return templateData;
  }
}
