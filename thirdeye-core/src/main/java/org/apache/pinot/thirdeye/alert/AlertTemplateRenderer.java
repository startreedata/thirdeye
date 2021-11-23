package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext.DEFAULT_TIME_FORMAT;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.plan.AnomalyDetectorPlanNode;
import org.apache.pinot.thirdeye.detection.v2.utils.DefaultTimeConverter;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.detection.TimeConverter;
import org.apache.pinot.thirdeye.util.GroovyTemplateUtils;

@Singleton
public class AlertTemplateRenderer {

  private static final String K_TIME_FORMAT = "timeFormat";
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
   * @param start start time
   * @param end end time
   * @return template populated with properties
   */
  public AlertTemplateDTO renderAlert(AlertApi alert, Date start, Date end)
      throws IOException, ClassNotFoundException {
    ensureExists(alert, ERR_OBJECT_DOES_NOT_EXIST, "alert body is null");

    if (alert.getId() != null) {
      final AlertDTO alertDto = ensureExists(alertManager.findById(alert.getId()));
      return renderAlert(alertDto, start, end);
    }

    final AlertTemplateApi templateApi = alert.getTemplate();
    ensureExists(templateApi, ERR_OBJECT_DOES_NOT_EXIST, "alert template body is null");
    final Map<String, Object> templateProperties = alert.getTemplateProperties();

    final AlertTemplateDTO alertTemplateDTO = ApiBeanMapper.toAlertTemplateDto(templateApi);
    return renderAlertInternal(alertTemplateDTO, templateProperties, start, end, alert.getName());
  }

  /**
   * Render the alert template for Alert task execution
   *
   * @param alert the alert DTO (persisted in db)
   * @param start start time
   * @param end end time
   * @return template populated with properties
   */

  public AlertTemplateDTO renderAlert(AlertDTO alert, Date start, Date end)
      throws IOException, ClassNotFoundException {
    return renderAlertInternal(
        alert.getTemplate(),
        alert.getTemplateProperties(),
        start,
        end,
        alert.getName());
  }

  private AlertTemplateDTO renderAlertInternal(final AlertTemplateDTO alertTemplateInsideAlertDto,
      final Map<String, Object> templateProperties,
      final Date start,
      final Date end,
      final String alertName)
      throws IOException, ClassNotFoundException {
    final AlertTemplateDTO template = getTemplate(alertTemplateInsideAlertDto);
    return applyContext(template,
        templateProperties,
        start,
        end,
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
      final Date startTime,
      final Date endTime,
      final String alertName) throws IOException, ClassNotFoundException {
    final Map<String, Object> properties = new HashMap<>();
    if (templateProperties != null) {
      properties.putAll(templateProperties);
    }

    final String timeFormat = findTimeFormat(template);
    final TimeConverter timeConverter = DefaultTimeConverter.get(timeFormat);

    properties.put("startTime", timeConverter.convertMillis(startTime.getTime()));
    properties.put("endTime", timeConverter.convertMillis(endTime.getTime()));
    // add source metadata to each node
    template.getNodes().stream()
        .filter(node -> node.getType().equals(AnomalyDetectorPlanNode.TYPE))
        .forEach(node -> node.getParams()
            .put("anomaly.source", String.format("%s/%s", alertName, node.getName())));

    final String jsonString = OBJECT_MAPPER.writeValueAsString(template);
    return GroovyTemplateUtils.applyContextToTemplate(jsonString,
        properties,
        AlertTemplateDTO.class);
  }

  private String findTimeFormat(final AlertTemplateDTO template) {
    for (PlanNodeBean node : template.getNodes()) {
      final Map<String, Object> params = node.getParams();
      if (params != null) {
        final Object value = params.get(K_TIME_FORMAT);
        if (value != null) {
          return value.toString();
        }
      }
    }

    return DEFAULT_TIME_FORMAT;
  }
}
