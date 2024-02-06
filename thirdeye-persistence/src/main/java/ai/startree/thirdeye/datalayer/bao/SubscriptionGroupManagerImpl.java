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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.Constants.METRICS_CACHE_TIMEOUT;
import static com.google.common.base.Suppliers.memoizeWithExpiration;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;

@Singleton
public class SubscriptionGroupManagerImpl extends
    AbstractManagerImpl<SubscriptionGroupDTO> implements SubscriptionGroupManager {

  @Inject
  public SubscriptionGroupManagerImpl(final GenericPojoDao genericPojoDao,
      final MetricRegistry metricRegistry) {
    super(SubscriptionGroupDTO.class, genericPojoDao);
    registerMetrics(metricRegistry);
  }

  private void registerMetrics(final MetricRegistry metricRegistry) {
    final Supplier<Number> notificationFlowsFun = () -> findAll().stream()
        .filter(sg -> CollectionUtils.isNotEmpty(sg.getAlertAssociations()))
        .filter(sg -> CollectionUtils.isNotEmpty(sg.getSpecs()))
        .map(sg -> sg.getAlertAssociations().size() * sg.getSpecs().size())
        .reduce(0, Integer::sum);
    
    Gauge.builder("thirdeye_notification_flows",
            memoizeWithExpiration( notificationFlowsFun,METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);

    metricRegistry.register("notificationFlowCountTotal",
        new CachedGauge<Number>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          public Number loadValue() {
            return notificationFlowsFun.get();
          }
        });
  }
}
