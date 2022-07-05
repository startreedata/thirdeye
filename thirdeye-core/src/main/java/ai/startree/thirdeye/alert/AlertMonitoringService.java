package ai.startree.thirdeye.alert;

import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertMonitoringService {

  private static final Logger log = LoggerFactory.getLogger(AlertMonitoringService.class);

  private final AlertManager alertManager;
  private final AlertTemplateRenderer renderer;

  @Inject
  public AlertMonitoringService(final AlertManager alertManager,
      final AlertTemplateRenderer renderer) {
    this.alertManager = alertManager;
    this.renderer = renderer;
  }

  public Set<MonitoredMetricWrapper> getUniqueMonitoredMetrics() {
    return alertManager.findAllActive().stream().map(alertDTO -> {
      try {
        // Interval does not have significance in this case, just a placeholder.
        return renderer.renderAlert(alertDTO, new Interval(1L, 2L)).getMetadata();
      } catch (IOException | ClassNotFoundException e) {
        log.warn("Trouble while rendering alert", e);
        return null;
      }
    }).filter(Objects::nonNull).map(metadata ->
      new MonitoredMetricWrapper()
          .setMetric(metadata.getMetric().getName())
          .setDataset(metadata.getDataset().getName())
          .setDatasource(metadata.getDatasource().getName())
    ).collect(Collectors.toSet());
  }
}
