/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.spi.Constants.METRICS_CACHE_TIMEOUT;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.spi.api.AppAnalyticsApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppAnalyticsService {

  private static final Logger log = LoggerFactory.getLogger(AppAnalyticsService.class);

  private final AlertManager alertManager;
  private final AlertTemplateRenderer renderer;
  private final AnomalyMetricsProvider anomalyMetricsProvider;
  private final AuthorizationManager authorizationManager;

  // FIXME CYRIL need to implement a cache with namespace key
  private final Supplier<Set<MonitoredMetricWrapper>> uniqueMonitoredMetricsSupplier =
      Suppliers.memoizeWithExpiration(this::getUniqueMonitoredMetrics,
          METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES)::get;

  @Inject
  public AppAnalyticsService(final AlertManager alertManager,
      final AlertTemplateRenderer renderer,
      final AnomalyMetricsProvider anomalyMetricsProvider,
      final AuthorizationManager authorizationManager
  ) {
    this.alertManager = alertManager;
    this.renderer = renderer;
    this.anomalyMetricsProvider = anomalyMetricsProvider;
    this.authorizationManager = authorizationManager;
    // fixme cyril authz global entity metrics should be maintained by DAOs
    Gauge.builder("thirdeye_active_distinct_metrics",
            () -> uniqueMonitoredMetricsSupplier.get().size())
        .register(Metrics.globalRegistry);
  }

  public String appVersion(final @Nullable ThirdEyePrincipal principal) {
    // this method does not require an identity for the moment - so the principal is not used 
    //  still enforcing principal as a parameter to respect architecture constraints 
    return AppAnalyticsService.class.getPackage().getImplementationVersion();
  }

  public AppAnalyticsApi getAppAnalytics(final ThirdEyePrincipal principal,
      final @Nullable Long startTime, final @Nullable Long endTime) {
    final AnomalyFilter filter = new AnomalyFilter()
        .setStartTimeIsGte(startTime)
        .setEndTimeIsLte(endTime);

    return new AppAnalyticsApi()
        .setVersion(appVersion(null))
        // FIXME CYRIL need authz filter
        .setnMonitoredMetrics(getUniqueMonitoredMetrics().size())
        .setAnomalyStats(anomalyMetricsProvider.computeAnomalyStats(principal, filter));
  }

  private Set<MonitoredMetricWrapper> getUniqueMonitoredMetrics() {
    // fixme cyril add authz
    return alertManager.findAllActive().stream()
        .map(this::getMetadata)
        .filter(Objects::nonNull)
        .map(this::wrapMonitoredMetric)
        .collect(Collectors.toSet());
  }

  private AlertMetadataDTO getMetadata(final AlertDTO alertDTO) {
    try {
      // Interval does not have significance in this case, just a placeholder.
      return renderer.renderAlert(alertDTO, new Interval(1L, 2L)).getMetadata();
    } catch (final IOException | ClassNotFoundException | BadRequestException e) {
      log.warn(String.format("Trouble while rendering alert, %s. id : %d",
          alertDTO.getName(),
          alertDTO.getId()), e);
      return null;
    }
  }

  private MonitoredMetricWrapper wrapMonitoredMetric(final AlertMetadataDTO metadata) {
    return new MonitoredMetricWrapper(metadata.getDatasource().getName(),
        metadata.getDataset().getDataset(), metadata.getMetric().getName());
  }

  private record MonitoredMetricWrapper(String datasource, String dataset, String metric) {}
}
