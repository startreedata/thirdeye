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

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class AlertManagerImpl extends AbstractManagerImpl<AlertDTO> implements
    AlertManager {

  @Inject
  public AlertManagerImpl(final GenericPojoDao genericPojoDao,
      final MetricRegistry metricRegistry) {
    super(AlertDTO.class, genericPojoDao);
    metricRegistry.register("activeAlertsCount",
        new CachedGauge<Long>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
      @Override
      public Long loadValue() {
        return countActive();
      }
    });
    metricRegistry.register("activeTimeseriesMonitoredCount", new CachedGauge<Integer>(15, TimeUnit.MINUTES) {
      @Override
      protected Integer loadValue() {
        final List<AlertDTO> activeAlerts = findAllActive();
        return activeAlerts.stream()
            // Assumes dangling enumeration items are handled and only linked items are present in DB
            .map(alert -> (int) genericPojoDao.count(Predicate.EQ("alertId", alert.getId()), EnumerationItemDTO.class))
            // add enumerationItems count if present, else just add 1 for simple alert
            .reduce(0, (tsCount, enumCount) -> tsCount + (enumCount == 0 ? 1 : enumCount));
      }
    });
  }

  @Override
  public int update(final AlertDTO alertDTO) {
    if (alertDTO.getId() == null) {
      final Long id = save(alertDTO);
      if (id > 0) {
        return 1;
      } else {
        return 0;
      }
    } else {
      return genericPojoDao.update(alertDTO);
    }
  }

  @Override
  public Long save(final AlertDTO alertDTO) {
    if (alertDTO.getId() != null) {
      //TODO: throw exception and force the caller to call update instead
      update(alertDTO);
      return alertDTO.getId();
    }

    final Long id = genericPojoDao.create(alertDTO);
    alertDTO.setId(id);
    return id;
  }

  @Override
  public List<AlertDTO> findAllActive() {
    return findByPredicate(Predicate.EQ("active", true));
  }

  public Long countActive() {
    return count(Predicate.EQ("active", true));
  }
}
