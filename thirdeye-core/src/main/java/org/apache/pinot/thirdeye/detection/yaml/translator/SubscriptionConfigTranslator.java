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

package org.apache.pinot.thirdeye.detection.yaml.translator;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionAlertRegistry;
import org.apache.pinot.thirdeye.detection.validators.SubscriptionConfigValidator;
import org.apache.pinot.thirdeye.spi.Constants.SubjectType;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;

/**
 * The translator converts the alert yaml config into a detection alert config
 */
public class SubscriptionConfigTranslator extends
    ConfigTranslator<SubscriptionGroupDTO, SubscriptionConfigValidator> {

  public static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  public static final String PROP_RECIPIENTS = "recipients";

  public static final String PROP_SUBS_GROUP_NAME = "subscriptionGroupName";
  public static final String PROP_CRON = "cron";
  public static final String PROP_ACTIVE = "active";
  public static final String PROP_APPLICATION = "application";
  public static final String PROP_FROM = "fromAddress";
  public static final String PROP_OWNERS = "owners";
  public static final String PROP_EMAIL_SUBJECT_TYPE = "emailSubjectStyle";
  public static final String PROP_ALERT_SCHEMES = "alertSchemes";
  public static final String PROP_DETECTION_NAMES = "subscribedDetections";
  public static final String PROP_TYPE = "type";
  public static final String PROP_CLASS_NAME = "className";
  public static final String PROP_PARAM = "params";
  public static final String DEFAULT_ALERTER_PIPELINE = "DEFAULT_ALERTER_PIPELINE";

  static final String PROP_ALERT_SUPPRESSORS = "alertSuppressors";
  static final String PROP_REFERENCE_LINKS = "referenceLinks";
  static final String PROP_TIME_WINDOWS = "timeWindows";
  // Every 5 minutes.
  static final String CRON_SCHEDULE_DEFAULT = "0 0/5 * * * ? *";

  private static final String PROP_DIMENSION = "dimension";
  private static final String PROP_DIMENSION_RECIPIENTS = "dimensionRecipients";
  private static final String PROP_SEVERITY_RECIPIENTS = "severityRecipients";

  private static final DetectionAlertRegistry DETECTION_ALERT_REGISTRY = DetectionAlertRegistry
      .getInstance();
  private static final Set<String> PROPERTY_KEYS = new HashSet<>(
      Arrays.asList(PROP_RECIPIENTS, PROP_DIMENSION, PROP_DIMENSION_RECIPIENTS,
          PROP_SEVERITY_RECIPIENTS));

  private final AlertManager detectionConfigDAO;

  public SubscriptionConfigTranslator(String yamlConfig, SubscriptionConfigValidator validator,
      final AlertManager detectionConfigManager) {
    super(yamlConfig, validator);
    detectionConfigDAO = detectionConfigManager;
  }

  private Map<String, Object> buildAlerterProperties(Map<String, Object> alertYamlConfigs,
      Collection<Long> detectionConfigIds) {
    Map<String, Object> properties = buildAlerterProperties(alertYamlConfigs);
    properties.put(PROP_DETECTION_CONFIG_IDS, detectionConfigIds);
    return properties;
  }

  private Map<String, Object> buildAlerterProperties(Map<String, Object> alertYamlConfigs) {
    Map<String, Object> properties = new HashMap<>();

    // Default subscription type is "DEFAULT_ALERTER_PIPELINE"
    alertYamlConfigs.putIfAbsent(PROP_TYPE, DEFAULT_ALERTER_PIPELINE);

    for (Map.Entry<String, Object> entry : alertYamlConfigs.entrySet()) {
      if (entry.getKey().equals(PROP_TYPE)) {
        properties.put(PROP_CLASS_NAME, DETECTION_ALERT_REGISTRY
            .lookupAlertFilters(MapUtils.getString(alertYamlConfigs, PROP_TYPE)));
      } else {
        if (PROPERTY_KEYS.contains(entry.getKey())) {
          properties.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return properties;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> buildAlertSuppressors(Map<String, Object> yamlAlertConfig) {
    List<Map<String, Object>> alertSuppressors = ConfigUtils
        .getList(yamlAlertConfig.get(PROP_ALERT_SUPPRESSORS));
    Map<String, Object> alertSuppressorsHolder = new HashMap<>();
    Map<String, Object> alertSuppressorsParsed = new HashMap<>();
    if (!alertSuppressors.isEmpty()) {
      for (Map<String, Object> alertSuppressor : alertSuppressors) {
        Map<String, Object> alertSuppressorsTimeWindow = new HashMap<>();
        if (alertSuppressor.get(PROP_TYPE) != null) {
          alertSuppressorsTimeWindow.put(PROP_CLASS_NAME,
              DETECTION_ALERT_REGISTRY
                  .lookupAlertSuppressors(alertSuppressor.get(PROP_TYPE).toString()));
        }

        if (alertSuppressor.get(PROP_PARAM) != null) {
          for (Map.Entry<String, Object> params : ((Map<String, Object>) alertSuppressor
              .get(PROP_PARAM)).entrySet()) {
            alertSuppressorsParsed.put(params.getKey(), params.getValue());
          }
        }

        String suppressorType =
            CaseFormat.UPPER_UNDERSCORE
                .to(CaseFormat.LOWER_CAMEL, alertSuppressor.get(PROP_TYPE).toString());
        alertSuppressorsTimeWindow
            .put(PROP_TIME_WINDOWS, new ArrayList<>(Arrays.asList(alertSuppressorsParsed)));
        alertSuppressorsHolder.put(suppressorType + "Suppressor", alertSuppressorsTimeWindow);
      }
    }

    return alertSuppressorsHolder;
  }

  @SuppressWarnings("unchecked")
  private NotificationSchemesDto buildNotificationSchemes(Map<String, Object> yamlAlertConfig) {
    NotificationSchemesDto notificationSchemesDto = new NotificationSchemesDto();
    List<Map<String, Object>> notificationSchemes = ConfigUtils
        .getList(yamlAlertConfig.get(PROP_ALERT_SCHEMES));
    if (!notificationSchemes.isEmpty()) {
      for (Map<String, Object> notificationScheme : notificationSchemes) {
        Preconditions.checkNotNull(notificationScheme.get(PROP_TYPE));
        if (notificationScheme.get(PROP_TYPE).toString().equals("EMAIL")) {
          if (notificationScheme.get(PROP_PARAM) != null) {
            Map<String, Object> schemeProps = (Map<String, Object>) ((Map<String, Object>) notificationScheme
                .get(PROP_PARAM)).get("recipients");
            notificationSchemesDto.setEmailScheme(new EmailSchemeDto()
                .setTo((List<String>) schemeProps.get("to"))
                .setCc((List<String>) schemeProps.get("cc")));
          }
        }
      }
    }

    return notificationSchemesDto;
  }

  /**
   * Generates the {@link SubscriptionGroupDTO} from the YAML Alert Map
   */
  @Override
  SubscriptionGroupDTO translateConfig() {
    Map<String, Object> yamlConfigMap = ConfigUtils.getMap(this.yaml.load(yamlConfig));

    SubscriptionGroupDTO alertConfigDTO = new SubscriptionGroupDTO();

    alertConfigDTO.setName(MapUtils.getString(yamlConfigMap, PROP_SUBS_GROUP_NAME));
    alertConfigDTO.setApplication(MapUtils.getString(yamlConfigMap, PROP_APPLICATION));
    alertConfigDTO.setFrom(MapUtils.getString(yamlConfigMap, PROP_FROM));
    List<String> owners = ConfigUtils.getList(yamlConfigMap.get(PROP_OWNERS));
    owners.replaceAll(String::trim);
    alertConfigDTO.setOwners(new ArrayList<>(new HashSet<>(owners)));

    alertConfigDTO
        .setCronExpression(MapUtils.getString(yamlConfigMap, PROP_CRON, CRON_SCHEDULE_DEFAULT));
    alertConfigDTO.setActive(MapUtils.getBooleanValue(yamlConfigMap, PROP_ACTIVE, true));
    alertConfigDTO.setYaml(yamlConfig);

    alertConfigDTO.setSubjectType(SubjectType.valueOf(
        (String) MapUtils.getObject(yamlConfigMap, PROP_EMAIL_SUBJECT_TYPE,
            SubjectType.METRICS.name())));

    Map<String, String> refLinks = ConfigUtils.getMap(yamlConfigMap.get(PROP_REFERENCE_LINKS));
    if (refLinks.isEmpty()) {
      refLinks.put("How to label Anomalies?", "https://go/howtolabel");
      refLinks.put("ThirdEye User Guide", "https://go/thirdeyeuserguide");
      refLinks.put("See how to add links", "https://go/thirdeyealertreflink");
    }
    alertConfigDTO.setRefLinks(ConfigUtils.getMap(yamlConfigMap.get(PROP_REFERENCE_LINKS)));

    alertConfigDTO.setNotificationSchemes(buildNotificationSchemes(yamlConfigMap));
    alertConfigDTO.setAlertSuppressors(buildAlertSuppressors(yamlConfigMap));

    // NOTE: The below fields will/should be hidden from the YAML/UI. They will only be updated by the backend pipeline.
    List<Long> detectionConfigIds = new ArrayList<>();
    List<String> detectionNames = ConfigUtils.getList(yamlConfigMap.get(PROP_DETECTION_NAMES));

    try {
      detectionConfigIds.addAll(
          detectionNames.stream().map(detectionName -> this.detectionConfigDAO.findByPredicate(
              Predicate.EQ("name", detectionName)).get(0).getId()).collect(Collectors.toList()));
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Cannot find detection pipeline, please check the subscribed detections.");
    }

    alertConfigDTO.setProperties(buildAlerterProperties(yamlConfigMap, detectionConfigIds));
    Map<Long, Long> vectorClocks = new HashMap<>();
    long currentTimestamp = System.currentTimeMillis();
    for (long detectionConfigId : detectionConfigIds) {
      vectorClocks.put(detectionConfigId, currentTimestamp);
    }
    alertConfigDTO.setVectorClocks(vectorClocks);

    return alertConfigDTO;
  }
}
