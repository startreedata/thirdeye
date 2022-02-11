/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert;

import ai.startree.thirdeye.detection.alert.suppress.DetectionAlertSuppressor;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.DataProvider;
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

  private final DataProvider provider;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager alertManager;

  @Inject
  public NotificationSchemeFactory(final DataProvider provider,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager) {
    this.provider = provider;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
  }

  public DetectionAlertFilter loadAlertFilter(SubscriptionGroupDTO alertConfig, long endTime)
      throws Exception {
    Preconditions.checkNotNull(alertConfig);
    String className = alertConfig.getProperties().get(PROP_CLASS_NAME).toString();
    LOG.debug("Loading Alert Filter : {}", className);
    Constructor<?> constructor = Class.forName(className)
        .getConstructor(DataProvider.class,
            SubscriptionGroupDTO.class,
            long.class,
            MergedAnomalyResultManager.class,
            AlertManager.class);
    return (DetectionAlertFilter) constructor.newInstance(provider,
        alertConfig,
        endTime,
        this.mergedAnomalyResultManager,
        this.alertManager);
  }

  public Set<DetectionAlertSuppressor> loadAlertSuppressors(SubscriptionGroupDTO alertConfig)
      throws Exception {
    Preconditions.checkNotNull(alertConfig);
    Set<DetectionAlertSuppressor> detectionAlertSuppressors = new HashSet<>();
    Map<String, Object> alertSuppressors = alertConfig.getAlertSuppressors();
    if (alertSuppressors == null || alertSuppressors.isEmpty()) {
      return detectionAlertSuppressors;
    }

    for (String alertSuppressor : alertSuppressors.keySet()) {
      LOG.debug("Loading Alert Suppressor : {}", alertSuppressor);
      Preconditions.checkNotNull(alertSuppressors.get(alertSuppressor));
      Preconditions.checkNotNull(
          ConfigUtils.getMap(alertSuppressors.get(alertSuppressor)).get(PROP_CLASS_NAME));
      Constructor<?> constructor = Class
          .forName(ConfigUtils.getMap(alertSuppressors.get(alertSuppressor))
              .get(PROP_CLASS_NAME).toString().trim())
          .getConstructor(SubscriptionGroupDTO.class, MergedAnomalyResultManager.class);
      detectionAlertSuppressors
          .add((DetectionAlertSuppressor) constructor.newInstance(alertConfig,
              this.mergedAnomalyResultManager));
    }

    return detectionAlertSuppressors;
  }

  public DetectionAlertFilterResult getDetectionAlertFilterResult(
      final SubscriptionGroupDTO subscriptionGroupDTO) throws Exception {
    // Load all the anomalies along with their recipients
    final DetectionAlertFilter alertFilter = loadAlertFilter(subscriptionGroupDTO,
        System.currentTimeMillis());
    DetectionAlertFilterResult result = alertFilter.run();

    // Suppress alerts if any and get the filtered anomalies to be notified
    final Set<DetectionAlertSuppressor> alertSuppressors = loadAlertSuppressors(subscriptionGroupDTO);
    for (final DetectionAlertSuppressor alertSuppressor : alertSuppressors) {
      result = alertSuppressor.run(result);
    }
    return result;
  }
}
