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
package ai.startree.thirdeye.notification;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.subscriptiongroup.filter.DetectionAlertFilter;
import ai.startree.thirdeye.subscriptiongroup.filter.DetectionAlertFilterResult;
import ai.startree.thirdeye.subscriptiongroup.filter.ToAllRecipientsDetectionAlertFilter;
import ai.startree.thirdeye.subscriptiongroup.suppress.DetectionAlertSuppressor;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NotificationSchemeFactory {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationSchemeFactory.class);

  private static final String PROP_CLASS_NAME = "className";

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager alertManager;

  @Inject
  public NotificationSchemeFactory(final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
  }

  public Set<DetectionAlertSuppressor> loadAlertSuppressors(
      final SubscriptionGroupDTO subscriptionGroup)
      throws Exception {
    Preconditions.checkNotNull(subscriptionGroup);
    final Set<DetectionAlertSuppressor> detectionAlertSuppressors = new HashSet<>();
    final Map<String, Object> alertSuppressors = subscriptionGroup.getAlertSuppressors();
    if (alertSuppressors == null || alertSuppressors.isEmpty()) {
      return detectionAlertSuppressors;
    }

    for (final String alertSuppressor : alertSuppressors.keySet()) {
      LOG.debug("Loading Alert Suppressor : {}", alertSuppressor);
      Preconditions.checkNotNull(alertSuppressors.get(alertSuppressor));
      Preconditions.checkNotNull(
          ConfigUtils.getMap(alertSuppressors.get(alertSuppressor)).get(PROP_CLASS_NAME));
      final Constructor<?> constructor = Class
          .forName(ConfigUtils.getMap(alertSuppressors.get(alertSuppressor))
              .get(PROP_CLASS_NAME).toString().trim())
          .getConstructor(SubscriptionGroupDTO.class, MergedAnomalyResultManager.class);
      detectionAlertSuppressors
          .add((DetectionAlertSuppressor) constructor.newInstance(subscriptionGroup,
              mergedAnomalyResultManager));
    }

    return detectionAlertSuppressors;
  }

  public DetectionAlertFilterResult getDetectionAlertFilterResult(
      final SubscriptionGroupDTO subscriptionGroupDTO) throws Exception {
    // Load all the anomalies along with their recipients
    requireNonNull(subscriptionGroupDTO, "subscription Group is null");
    final DetectionAlertFilter alertFilter = new ToAllRecipientsDetectionAlertFilter(
        subscriptionGroupDTO,
        System.currentTimeMillis(),
        mergedAnomalyResultManager,
        alertManager);
    DetectionAlertFilterResult result = alertFilter.run();

    // Suppress alerts if any and get the filtered anomalies to be notified
    final Set<DetectionAlertSuppressor> alertSuppressors = loadAlertSuppressors(subscriptionGroupDTO);
    for (final DetectionAlertSuppressor alertSuppressor : alertSuppressors) {
      result = alertSuppressor.run(result);
    }
    return result;
  }
}
