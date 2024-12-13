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

import static ai.startree.thirdeye.spi.util.MetricsUtils.scheduledRefreshSupplier;

import ai.startree.thirdeye.datalayer.DatabaseClient;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertManagerImpl extends AbstractManagerImpl<AlertDTO> implements
    AlertManager {
  
  private static final Logger LOG = LoggerFactory.getLogger(AlertManagerImpl.class);
  private static final String COUNT_ACTIVE_TIMESERIES_QUERY = """
  with t as (
    SELECT
      detection_config_index.base_id,
      COUNT(enumeration_item_index.base_id) AS c
    FROM detection_config_index
      LEFT JOIN enumeration_item_index ON detection_config_index.base_id = alert_id
    WHERE detection_config_index.active
    GROUP BY detection_config_index.base_id
    )
    select SUM(CASE WHEN c < 1 THEN 1 else c END ) from t;
  """;

  // TODO CYRIL introduce JOOQ 
  private final DatabaseClient databaseClient;

  @Inject
  public AlertManagerImpl(final GenericPojoDao genericPojoDao, final DatabaseClient databaseClient) {
    super(AlertDTO.class, genericPojoDao);
    this.databaseClient = databaseClient;
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
            scheduledRefreshSupplier(this::countActive, Duration.ofMinutes(1)))
        .register(Metrics.globalRegistry);
    Gauge.builder("thirdeye_active_timeseries",
            scheduledRefreshSupplier(this::countActiveTimeseries, Duration.ofMinutes(1)))
        .register(Metrics.globalRegistry);
    LOG.info("Registered alert database metrics.");
  }
  
  private long countActiveTimeseries() {
    try {
      return databaseClient.executeTransaction(connection -> {
        try (final Statement s = connection.createStatement();
            final ResultSet rs = s.executeQuery(COUNT_ACTIVE_TIMESERIES_QUERY)
        ) {
          if (rs.next()) {
            return rs.getLong(1);
          } else {
            return 0L;
          }
        }
      });
    } catch (Exception e) {
      LOG.error("Failed to compute the number of active timeseries from the database. Returning -1.", e);
      return -1;
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
