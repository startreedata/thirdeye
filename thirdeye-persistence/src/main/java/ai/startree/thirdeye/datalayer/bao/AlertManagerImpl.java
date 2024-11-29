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
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertManagerImpl extends AbstractManagerImpl<AlertDTO> implements
    AlertManager {
  
  private static final Logger LOG = LoggerFactory.getLogger(AlertManagerImpl.class);

  @Inject
  public AlertManagerImpl(final GenericPojoDao genericPojoDao) {
    super(AlertDTO.class, genericPojoDao);
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
  public void registerDatabaseMetrics() {
    Gauge.builder("thirdeye_active_alerts",
            memoizeWithExpiration(this::countActive, METRICS_CACHE_TIMEOUT.toMinutes(),
                TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);

    final Supplier<Number> activeTimeseriesCountFun = () -> {
      final List<AlertDTO> activeAlerts = findAllActive();
      return activeAlerts.stream()
          // Assumes dangling enumeration items are handled and only linked items are present in DB
          .map(alert -> (int) genericPojoDao.count(Predicate.EQ("alertId", alert.getId()),
              EnumerationItemDTO.class))
          // add enumerationItems count if present, else just add 1 for simple alert
          .reduce(0, (tsCount, enumCount) -> tsCount + (enumCount == 0 ? 1 : enumCount));
    };
    Gauge.builder("thirdeye_active_timeseries",
            memoizeWithExpiration(activeTimeseriesCountFun, 15, TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);
    LOG.info("Registered alert database metrics.");
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

  @Override
  public List<AlertDTO> findAllActiveInNamespace(final @Nullable String namespace) {
    return
        findByPredicate(
            Predicate.AND(
                Predicate.EQ("active", true),
                Predicate.OR(
                    Predicate.EQ("namespace", namespace),
                    // existing entities are not migrated automatically so they can have their namespace column to null in the index table, even if they do belong to a namespace 
                    //  todo cyril authz - ensure all entities are eventually migrated - then remove this
                    Predicate.EQ("namespace", null)
                )
            ))
        // we still need to perform in-app filtering until all entities namespace are migrated in db - see above 
        .stream().filter(e -> Objects.equals(e.namespace(), namespace)).toList();
  }

  public Long countActive() {
    return count(Predicate.EQ("active", true));
  }
}
