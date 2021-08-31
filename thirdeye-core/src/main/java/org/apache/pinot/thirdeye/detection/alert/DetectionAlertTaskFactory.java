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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.detection.alert.scheme.DetectionAlertScheme;
import org.apache.pinot.thirdeye.detection.alert.scheme.EmailAlertScheme;
import org.apache.pinot.thirdeye.detection.alert.scheme.WebhookAlertScheme;
import org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertSuppressor;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionAlertTaskFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionAlertTaskFactory.class);

  private static final String PROP_CLASS_NAME = "className";

  private final DataProvider provider;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager alertManager;
  private final WebhookAlertScheme webhookAlertScheme;
  private final EmailAlertScheme emailAlertScheme;

  @Inject
  public DetectionAlertTaskFactory(final DataProvider provider,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager,
      final EmailAlertScheme emailAlertScheme,
      final WebhookAlertScheme webhookAlertScheme) {
    this.provider = provider;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
    this.webhookAlertScheme = webhookAlertScheme;
    this.emailAlertScheme = emailAlertScheme;
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

  public Set<DetectionAlertScheme> getAlertSchemes()
      throws Exception {
    return ImmutableSet.of(emailAlertScheme, webhookAlertScheme);
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
}
