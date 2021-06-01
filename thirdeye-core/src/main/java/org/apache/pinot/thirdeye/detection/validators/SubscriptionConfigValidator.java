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

package org.apache.pinot.thirdeye.detection.validators;

import static org.apache.pinot.thirdeye.detection.yaml.translator.SubscriptionConfigTranslator.PROP_APPLICATION;
import static org.apache.pinot.thirdeye.detection.yaml.translator.SubscriptionConfigTranslator.PROP_CRON;
import static org.apache.pinot.thirdeye.detection.yaml.translator.SubscriptionConfigTranslator.PROP_DETECTION_NAMES;

import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Application specific constraints and validations on subscription group are defined here
 */
public class SubscriptionConfigValidator extends ThirdEyeUserConfigValidator<SubscriptionGroupDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriptionConfigValidator.class);
  private static final String DEFAULT_SUBSCRIPTION_CONFIG_SCHEMA_PATH =
      "/validators/subscription/subscription-config-schema.json";
  private final AlertManager detectionConfigManager;
  private final ApplicationManager applicationManager;

  public SubscriptionConfigValidator(final AlertManager detectionConfigManager,
      final ApplicationManager applicationManager) {
    super(DEFAULT_SUBSCRIPTION_CONFIG_SCHEMA_PATH);
    this.detectionConfigManager = detectionConfigManager;
    this.applicationManager = applicationManager;
  }

  /**
   * Perform validation on the parsed & constructed subscription config
   */
  @Override
  public void semanticValidation(SubscriptionGroupDTO alertConfig)
      throws ConfigValidationException {
    // TODO
  }

  /**
   * Perform validations on the user specified subscription yaml configuration
   *
   * @param config subscription yaml configuration to be validated
   */
  @Override
  public void staticValidation(String config) throws ConfigValidationException {
    Map<String, Object> subscriptionConfigMap = ConfigUtils.getMap(new Yaml().load(config));
    if (subscriptionConfigMap.containsKey(PROP_DISABLE_VALD) && MapUtils
        .getBoolean(subscriptionConfigMap, PROP_DISABLE_VALD)) {
      LOG.info("Validation disabled for subscription config " + config);
      return;
    }

    super.schemaValidation(config);

    // Make sure the subscribed detections exist
    List<String> detectionNames = ConfigUtils
        .getList(subscriptionConfigMap.get(PROP_DETECTION_NAMES));
    for (String detectionName : detectionNames) {
      ConfigValidationUtils.checkArgument(!detectionConfigManager
              .findByPredicate(Predicate.EQ("name", detectionName)).isEmpty(),
          "Cannot find detection " + detectionName + " - Please ensure the detections listed under "
              + PROP_DETECTION_NAMES + " exist and are correctly configured.");
    }

    // application should exist in our registry
    String applicationName = MapUtils.getString(subscriptionConfigMap, PROP_APPLICATION);
    ConfigValidationUtils.checkArgument(
        !applicationManager.findByName(applicationName).isEmpty(),
        "Application name doesn't exist in our registry. Please use an existing application name or"
            + " reach out to the ThirdEye team to setup a new one.");

    // make sure the specified cron is valid
    String cron = MapUtils.getString(subscriptionConfigMap, PROP_CRON);
    if (cron != null) {
      ConfigValidationUtils.checkArgument(CronExpression.isValidExpression(cron), "The cron"
          + " specified in the subscription group is incorrect. Please verify using an online cron maker.");
    }
  }
}
