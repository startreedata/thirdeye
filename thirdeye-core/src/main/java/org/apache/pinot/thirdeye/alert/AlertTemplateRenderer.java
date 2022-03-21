package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.plan.AnomalyDetectorPlanNode;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.util.GroovyTemplateUtils;

@Singleton
public class AlertTemplateRenderer {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
   * @param startMillis start time
   * @param endMillis end time
   * @return template populated with properties
   */
  public AlertTemplateDTO renderAlert(AlertApi alert, long startMillis, long endMillis)
      throws IOException, ClassNotFoundException {
    ensureExists(alert, ERR_OBJECT_DOES_NOT_EXIST, "alert body is null");

    if (alert.getId() != null) {
      final AlertDTO alertDto = ensureExists(alertManager.findById(alert.getId()));
      return renderAlert(alertDto, startMillis, endMillis);
    }

    final AlertTemplateApi templateApi = alert.getTemplate();
    ensureExists(templateApi, ERR_OBJECT_DOES_NOT_EXIST, "alert template body is null");
    final Map<String, Object> templateProperties = alert.getTemplateProperties();

    final AlertTemplateDTO alertTemplateDTO = ApiBeanMapper.toAlertTemplateDto(templateApi);
    return renderAlertInternal(alertTemplateDTO,
        templateProperties,
        startMillis,
        endMillis,
        alert.getName());
  }

  /**
   * Render the alert template for Alert task execution
   *
   * @param alert the alert DTO (persisted in db)
   * @param startMillis start time
   * @param endMillis end time
   * @return template populated with properties
   */
  public AlertTemplateDTO renderAlert(AlertDTO alert, long startMillis, long endMillis)
      throws IOException, ClassNotFoundException {
    return renderAlertInternal(
        alert.getTemplate(),
        alert.getTemplateProperties(),
        startMillis,
        endMillis,
        alert.getName());
  }

  private AlertTemplateDTO renderAlertInternal(final AlertTemplateDTO alertTemplateInsideAlertDto,
      final Map<String, Object> templateProperties,
      final long startMillis,
      final long endMillis,
      final String alertName)
      throws IOException, ClassNotFoundException {
    final AlertTemplateDTO template = getTemplate(alertTemplateInsideAlertDto);
    return applyContext(template,
        templateProperties,
        startMillis,
        endMillis,
        alertName);
  }

  private AlertTemplateDTO getTemplate(final AlertTemplateDTO alertTemplateDTO) {
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
      final long startTimeMillis,
      final long endTimeMillis,
      final String alertName) throws IOException, ClassNotFoundException {
    final Map<String, Object> properties = new HashMap<>();
    if (templateProperties != null) {
      properties.putAll(templateProperties);
    }

    properties.put("startTime", startTimeMillis);
    properties.put("endTime", endTimeMillis);
    // add source metadata to each node
    if (template.getNodes() != null) {
      template.getNodes().stream()
          .filter(node -> node.getType().equals(AnomalyDetectorPlanNode.TYPE))
          .forEach(node -> node.getParams()
              .put("anomaly.source", String.format("%s/%s", alertName, node.getName())));
    }

    final String jsonString = OBJECT_MAPPER.writeValueAsString(template);
    return GroovyTemplateUtils.applyContextToTemplate(jsonString,
        properties,
        AlertTemplateDTO.class);
  }
}
