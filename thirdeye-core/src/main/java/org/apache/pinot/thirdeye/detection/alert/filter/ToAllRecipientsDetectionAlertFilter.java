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

package org.apache.pinot.thirdeye.detection.alert.filter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.StatefulDetectionAlertFilter;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.detection.annotation.AlertFilter;

/**
 * The detection alert filter that sends the anomaly email to all recipients
 */
@AlertFilter(type = "DEFAULT_ALERTER_PIPELINE")
public class ToAllRecipientsDetectionAlertFilter extends StatefulDetectionAlertFilter {

  public static final String PROP_RECIPIENTS = "recipients";
  public static final String PROP_TO = "to";
  public static final String PROP_CC = "cc";
  public static final String PROP_BCC = "bcc";
  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";

  final Map<String, List<String>> recipients;
  final List<Long> alertIds;

  public ToAllRecipientsDetectionAlertFilter(final DataProvider provider,
      final SubscriptionGroupDTO config,
      final long endTime,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager) {
    super(provider, config, endTime, mergedAnomalyResultManager,
        detectionConfigManager);

    final Map<String, Object> properties = this.config.getProperties();
    recipients = ConfigUtils.getMap(properties.get(PROP_RECIPIENTS));
    alertIds = ConfigUtils.getLongs(properties.get(PROP_DETECTION_CONFIG_IDS));
  }

  @Override
  public DetectionAlertFilterResult run() {
    final DetectionAlertFilterResult result = new DetectionAlertFilterResult();

    // Fetch all the anomalies to be notified to the recipients
    final Set<MergedAnomalyResultDTO> anomalies = filter(makeVectorClocks(alertIds));

    // Handle legacy recipients yaml syntax
    if (SubscriptionUtils.isEmptyEmailRecipients(config) && CollectionUtils
        .isNotEmpty(recipients.get(PROP_TO))) {
      // recipients are configured using the older syntax
      config.setNotificationSchemes(generateNotificationSchemeProps(
          config,
          recipients.get(PROP_TO),
          recipients.get(PROP_CC),
          recipients.get(PROP_BCC)));
    }

    return result.addMapping(new DetectionAlertFilterNotification(config), anomalies);
  }
}
