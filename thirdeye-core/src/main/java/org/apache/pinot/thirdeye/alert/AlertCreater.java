package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.detection.DataProvider;

@Singleton
public class AlertCreater {

  private final DataProvider dataProvider;
  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final AlertManager alertManager;

  @Inject
  public AlertCreater(
      final DataProvider dataProvider,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final AlertManager alertManager) {
    this.dataProvider = dataProvider;
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.alertManager = alertManager;
  }

  public Long create(AlertApi api) {
    final AlertDTO dto = toAlertDTO(api);

    final Long id = alertManager.save(dto);
    return id;
  }

  private AlertDTO toAlertDTO(final AlertApi api) {
    final AlertDTO dto = new AlertDTO();

    dto.setName(api.getName());
    dto.setDescription(api.getDescription());
    dto.setActive(true);
    dto.setCron(api.getCron());
    dto.setLastTimestamp(optional(api.getLastTimestamp())
        .map(d -> d.toInstant().toEpochMilli())
        .orElse(0L));
    dto.setUpdateTime(new Timestamp(System.currentTimeMillis()));

    return dto;
  }
}
