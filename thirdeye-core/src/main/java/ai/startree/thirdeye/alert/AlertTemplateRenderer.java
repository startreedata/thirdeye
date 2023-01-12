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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import ai.startree.thirdeye.util.StringTemplateUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;

@Singleton
public class AlertTemplateRenderer {

  private final AlertManager alertManager;
  private final AlertTemplateManager alertTemplateManager;

  @Inject
  public AlertTemplateRenderer(
      final AlertManager alertManager,
      final AlertTemplateManager alertTemplateManager) {
    this.alertManager = alertManager;
    this.alertTemplateManager = alertTemplateManager;
  }

  /**
   * Render the alert template API for /evaluate
   *
   * @param alert the alert API
   * @param detectionInterval interval of detection
   * @return template populated with properties
   */
  public AlertTemplateDTO renderAlert(AlertApi alert, final Interval detectionInterval)
      throws IOException, ClassNotFoundException {
    ensureExists(alert, ERR_OBJECT_DOES_NOT_EXIST, "alert body is null");

    if (alert.getId() != null) {
      final AlertDTO alertDto = ensureExists(alertManager.findById(alert.getId()));
      return renderAlert(alertDto, detectionInterval);
    }

    final AlertTemplateApi templateApi = alert.getTemplate();
    ensureExists(templateApi, ERR_OBJECT_DOES_NOT_EXIST, "alert template body is null");
    final Map<String, Object> templateProperties = alert.getTemplateProperties();

    final AlertTemplateDTO alertTemplateDTO = ApiBeanMapper.toAlertTemplateDto(templateApi);
    return renderAlertInternal(alertTemplateDTO,
        templateProperties,
        detectionInterval,
        alert.getName());
  }

  /**
   * Render the alert template for Alert task execution
   *
   * @param alert the alert DTO (persisted in db)
   * @param detectionInterval interval of detection
   * @return template populated with properties
   */
  public AlertTemplateDTO renderAlert(AlertDTO alert, final Interval detectionInterval)
      throws IOException, ClassNotFoundException {
    return renderAlertInternal(
        alert.getTemplate(),
        alert.getTemplateProperties(),
        detectionInterval,
        alert.getName());
  }

  /**
   * Render the alert with an enumeration Item.
   *
   * Render the template properties, then render the enumeration item properties.
   */
  public AlertTemplateDTO renderAlert(final AlertDTO alert, final Interval detectionInterval,
      @Nullable final EnumerationItemDTO enumerationItemDTO)
      throws IOException, ClassNotFoundException {
    final AlertTemplateDTO templateWithAlertProperties = renderAlert(alert, detectionInterval);

    if (enumerationItemDTO == null || enumerationItemDTO.getParams() == null
        || enumerationItemDTO.getParams().isEmpty()) {
      return templateWithAlertProperties;
    }

    // re-render with enum properties
    // remove id and name to prevent template being re-fetched from db
    final Long templateId = templateWithAlertProperties.getId();
    final String templateName = templateWithAlertProperties.getName();
    templateWithAlertProperties.setId(null);
    templateWithAlertProperties.setName(null);
    final AlertDTO alertWithEnumProperties = new AlertDTO().setTemplate(templateWithAlertProperties)
        .setTemplateProperties(enumerationItemDTO.getParams());
    final AlertTemplateDTO templateWithEnumProperties = renderAlert(alertWithEnumProperties,
        detectionInterval);
    templateWithEnumProperties.setId(templateId);
    templateWithEnumProperties.setName(templateName);
    return templateWithEnumProperties;
  }

  private AlertTemplateDTO renderAlertInternal(final AlertTemplateDTO alertTemplateInsideAlertDto,
      final Map<String, Object> templateProperties,
      final Interval detectionInterval,
      final String alertName)
      throws IOException, ClassNotFoundException {
    final AlertTemplateDTO template = getTemplate(alertTemplateInsideAlertDto);
    return applyContext(template,
        templateProperties,
        detectionInterval,
        alertName);
  }

  public AlertTemplateDTO getTemplate(final AlertTemplateDTO alertTemplateDTO) {
    final Long id = alertTemplateDTO.getId();
    if (id != null) {
      return alertTemplateManager.findById(id);
    }

    final String name = alertTemplateDTO.getName();
    if (name != null) {
      final List<AlertTemplateDTO> byName = alertTemplateManager.findByName(name);
      ensure(byName.size() == 1, ERR_OBJECT_DOES_NOT_EXIST, "template not found: " + name);
      return byName.get(0);
    }

    return alertTemplateDTO;
  }

  private AlertTemplateDTO applyContext(final AlertTemplateDTO template,
      final Map<String, Object> templateProperties,
      final Interval detectionInterval,
      final String alertName) throws IOException, ClassNotFoundException {
    final Map<String, Object> properties = new HashMap<>();
    // legacy properties can be removed once all users have migrated their template to the new propertiesMetadata
    final Map<String, Object> legacyDefaultProperties = optional(
        template.getDefaultProperties()).orElse(new HashMap<>());
    properties.putAll(legacyDefaultProperties);
    final Map<String, Object> defaultProperties = defaultProperties(template.getProperties());
    properties.putAll(defaultProperties);

    if (templateProperties != null) {
      properties.putAll(templateProperties);
    }

    properties.put("startTime", detectionInterval.getStartMillis());
    properties.put("endTime", detectionInterval.getEndMillis());
    // add source metadata to each node
    if (template.getNodes() != null) {
      template.getNodes().stream()
          // TODO spyne remove magic string. This was done to remove dependency of AnomalyDetector.TYPE on the renderer
          .filter(node -> node.getType().equals("AnomalyDetector"))
          .forEach(node -> node.getParams()
              .putValue("anomaly.source", String.format("%s/%s", alertName, node.getName())));
    }

    return StringTemplateUtils.applyContext(template, properties);
  }

  private @NonNull Map<String, Object> defaultProperties(
      final @Nullable List<TemplatePropertyMetadata> propertiesMetadata) {
    final HashMap<String, Object> res = new HashMap<>();
    if (propertiesMetadata == null) {
      return res;
    }
    for (final TemplatePropertyMetadata p : propertiesMetadata) {
      if (optional(p.isDefaultIsNull()).orElse(false)) {
        res.put(p.getName(), null);
      } else if (p.getDefaultValue() != null) {
        res.put(p.getName(), p.getDefaultValue());
      }
    }

    return res;
  }
}
