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
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.api.AppAnalyticsApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

  public Supplier<Set<MonitoredMetricWrapper>> uniqueMonitoredMetricsSupplier =
      Suppliers.memoizeWithExpiration(this::getUniqueMonitoredMetrics,
          METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES)::get;

  @Inject
  public AppAnalyticsService(final AlertManager alertManager,
      final AlertTemplateRenderer renderer,
      final MetricRegistry metricRegistry,
      final AnomalyMetricsProvider anomalyMetricsProvider) {
    this.alertManager = alertManager;
    this.renderer = renderer;
    this.anomalyMetricsProvider = anomalyMetricsProvider;
    registerMetrics(metricRegistry);
  }

  public static String appVersion() {
    return AppAnalyticsService.class.getPackage().getImplementationVersion();
  }

  private void registerMetrics(final MetricRegistry metricRegistry) {
    Gauge.builder("thirdeye_active_distinct_metrics", this::uniqueMonitoredMetricsCount)
        .register(Metrics.globalRegistry);
    // deprecated - use thirdeye_active_distinct_metrics
    metricRegistry.register("nMonitoredMetrics",
        new CachedGauge<Integer>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Integer loadValue() {
            return uniqueMonitoredMetricsCount();
          }
        });
  }

  public Integer uniqueMonitoredMetricsCount() {
    return uniqueMonitoredMetricsSupplier.get().size();
  }

  private Set<MonitoredMetricWrapper> getUniqueMonitoredMetrics() {
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

  public AppAnalyticsApi getAppAnalytics(final Long startTime, final Long endTime) {
    final List<Predicate> predicates = new ArrayList<>();
    optional(startTime).ifPresent(start -> predicates.add(Predicate.GE("startTime", startTime)));
    optional(endTime).ifPresent(end -> predicates.add(Predicate.LE("endTime", endTime)));
    final Predicate predicate = predicates.isEmpty()
        ? null : Predicate.AND(predicates.toArray(Predicate[]::new));
    return new AppAnalyticsApi()
        .setVersion(appVersion())
        .setnMonitoredMetrics(uniqueMonitoredMetricsCount())
        .setAnomalyStats(anomalyMetricsProvider.computeAnomalyStats(predicate));
  }

  public record MonitoredMetricWrapper(String datasource, String dataset, String metric) {}
}
