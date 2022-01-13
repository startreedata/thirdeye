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

package org.apache.pinot.thirdeye.detection.alert;

import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.spi.Constants;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResultSource;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;


public abstract class StatefulDetectionAlertFilter extends DetectionAlertFilter {

  public static final String PROP_TO = "to";
  public static final String PROP_CC = "cc";
  public static final String PROP_BCC = "bcc";
  public static final String PROP_RECIPIENTS = "recipients";

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager detectionConfigManager;

  public StatefulDetectionAlertFilter(DataProvider provider, SubscriptionGroupDTO config,
      long endTime, final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager) {
    super(provider, config, endTime);
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.detectionConfigManager = detectionConfigManager;
  }

  protected final Set<MergedAnomalyResultDTO> filter(Map<Long, Long> vectorClocks) {
    // retrieve all candidate anomalies
    Set<MergedAnomalyResultDTO> allAnomalies = new HashSet<>();
    for (Long detectionId : vectorClocks.keySet()) {
      // Ignore disabled detections
      AlertDTO detection = detectionConfigManager
          .findById(detectionId);
      if (detection == null || !detection.isActive()) {
        continue;
      }

      // No point in fetching anomalies older than MAX_ANOMALY_NOTIFICATION_LOOKBACK
      long startTime = vectorClocks.get(detectionId);
      if (startTime < this.endTime - Constants.ANOMALY_NOTIFICATION_LOOKBACK_TIME) {
        startTime = this.endTime - Constants.ANOMALY_NOTIFICATION_LOOKBACK_TIME;
      }

      Collection<MergedAnomalyResultDTO> candidates = mergedAnomalyResultManager
          .findByCreatedTimeInRangeAndDetectionConfigId(startTime + 1, this.endTime, detectionId);

      long finalStartTime = startTime;
      Collection<MergedAnomalyResultDTO> anomalies =
          Collections2.filter(candidates, anomaly -> anomaly != null && !anomaly.isChild()
              && !AlertUtils.hasFeedback(anomaly)
              && anomaly.getCreatedTime() > finalStartTime
              && (anomaly.getAnomalyResultSource()
              .equals(AnomalyResultSource.DEFAULT_ANOMALY_DETECTION) ||
              anomaly.getAnomalyResultSource()
                  .equals(AnomalyResultSource.DATA_QUALITY_DETECTION) ||
              anomaly.getAnomalyResultSource()
                  .equals(AnomalyResultSource.ANOMALY_REPLAY)));

      allAnomalies.addAll(anomalies);
    }
    return allAnomalies;
  }

  protected final Map<Long, Long> makeVectorClocks(Collection<Long> detectionConfigIds) {
    Map<Long, Long> clocks = new HashMap<>();

    for (Long id : detectionConfigIds) {
      clocks.put(id, MapUtils.getLong(this.config.getVectorClocks(), id, 0L));
    }

    return clocks;
  }

  protected Set<String> cleanupRecipients(Set<String> recipient) {
    Set<String> filteredRecipients = new HashSet<>();
    if (recipient != null) {
      filteredRecipients.addAll(recipient);
      filteredRecipients = filteredRecipients.stream().map(String::trim)
          .collect(Collectors.toSet());
      filteredRecipients.removeIf(rec -> rec == null || "".equals(rec));
    }
    return filteredRecipients;
  }

  /**
   * Extracts the alert schemes from config and also merges (overrides)
   * recipients explicitly defined outside the scope of alert schemes.
   */
  protected NotificationSchemesDto generateNotificationSchemeProps(SubscriptionGroupDTO config,
      List<String> to, List<String> cc, List<String> bcc) {
    NotificationSchemesDto notificationSchemeProps = new NotificationSchemesDto();
    notificationSchemeProps.setWebhookScheme(config.getNotificationSchemes().getWebhookScheme());
    // Override the email alert scheme
    EmailSchemeDto emailScheme = new EmailSchemeDto()
        .setCc(cc)
        .setTo(to)
        .setBcc(bcc);
    notificationSchemeProps.setEmailScheme(emailScheme);
    return notificationSchemeProps;
  }
}
