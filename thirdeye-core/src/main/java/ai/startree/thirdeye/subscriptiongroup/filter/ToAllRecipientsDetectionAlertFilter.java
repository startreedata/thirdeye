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
package ai.startree.thirdeye.subscriptiongroup.filter;

import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.annotation.AlertFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    return result.addMapping(new DetectionAlertFilterNotification(config), anomalies);
  }
}
