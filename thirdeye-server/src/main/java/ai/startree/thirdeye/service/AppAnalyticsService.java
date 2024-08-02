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
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppAnalyticsService {

  private static final Logger log = LoggerFactory.getLogger(AppAnalyticsService.class);

  private final AlertManager alertManager;
  private final AlertTemplateRenderer renderer;
  private final AuthorizationManager authorizationManager;
  private final AnomalyManager anomalyDao;

  @Inject
  public AppAnalyticsService(final AlertManager alertManager, final AlertTemplateRenderer renderer,
      final AuthorizationManager authorizationManager, final AnomalyManager anomalyManager) {
    this.alertManager = alertManager;
    this.renderer = renderer;
    this.authorizationManager = authorizationManager;
    this.anomalyDao = anomalyManager;
    // todo cyril authz global entity metrics should be maintained by DAOs - this one requires access to the alertTemplate manager though
    Gauge.builder("thirdeye_active_distinct_metrics",
            Suppliers.memoizeWithExpiration(this::getUniqueMonitoredMetrics,
                METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);
  }

  public String appVersion(final @Nullable ThirdEyePrincipal principal) {
    // this method does not require an identity for the moment - so the principal is not used 
    //  still enforcing principal as a parameter to respect architecture constraints 
    return AppAnalyticsService.class.getPackage().getImplementationVersion();
  }

  public AppAnalyticsApi getAppAnalytics(final ThirdEyePrincipal principal,
      final @Nullable Long startTime, final @Nullable Long endTime) {
    final AnomalyFilter filter = new AnomalyFilter().setStartTimeIsGte(startTime)
        .setEndTimeIsLte(endTime);

    final @Nullable String namespace = authorizationManager.currentNamespace(principal);

    // ensure the user has read access to alert and entities
    // todo cyril authz - usage of dummy entities for access check - avoid by using the alertService
    authorizationManager.ensureCanRead(principal, new AlertDTO().setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace)));
    authorizationManager.ensureCanRead(principal, new AnomalyDTO().setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace)));

    return new AppAnalyticsApi().setVersion(appVersion(null))
        .setnMonitoredMetrics(getUniqueMonitoredMetricsInNamespace(namespace))
        .setAnomalyStats(anomalyDao.anomalyStats(namespace, filter));
  }

  private int getUniqueMonitoredMetricsInNamespace(final @Nullable String namespace) {
    return Math.toIntExact(alertManager.findAllActiveInNamespace(namespace)
        .stream()
        .map(alertDTO -> renderer.renderAlert(alertDTO).getMetadata())
        .filter(Objects::nonNull)
        .map(MonitoredMetricKey::fromMetadata)
        .distinct().count());
  }
  
  // for internal use only - see getUniqueMonitoredMetricsWithNamespace
  private int getUniqueMonitoredMetrics() {
    return Math.toIntExact(alertManager.findAllActive()
        .stream()
        .map(alertDTO -> renderer.renderAlert(alertDTO).getMetadata())
        .filter(Objects::nonNull)
        .map(MonitoredMetricKey::fromMetadata)
        .distinct().count());
  }

  private record MonitoredMetricKey(String datasource, String dataset, String metric) {

    public static MonitoredMetricKey fromMetadata(final AlertMetadataDTO metadata) {
      return new MonitoredMetricKey(metadata.getDatasource().getName(),
          metadata.getDataset().getDataset(), metadata.getMetric().getName());
    }
  }
}
