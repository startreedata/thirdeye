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
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RcaInvestigationManagerImpl extends AbstractManagerImpl<RcaInvestigationDTO> implements
    RcaInvestigationManager {

  private static final Logger LOG = LoggerFactory.getLogger(RcaInvestigationManagerImpl.class);

  @Inject
  public RcaInvestigationManagerImpl(final GenericPojoDao genericPojoDao) {
    super(RcaInvestigationDTO.class, genericPojoDao);
  }

  @Override
  public List<RcaInvestigationDTO> findByName(String name) {
    return findByPredicate(Predicate.EQ("name", name));
  }

  @Override
  public List<RcaInvestigationDTO> findByOwner(String owner) {
    return findByPredicate(Predicate.EQ("owner", owner));
  }

  @Override
  public List<RcaInvestigationDTO> findByAnomalyRange(long start, long end) {
    return findByPredicate(Predicate
        .AND(Predicate.GT("anomalyRangeEnd", start), Predicate.LT("anomalyRangeStart", end)));
  }

  @Override
  public List<RcaInvestigationDTO> findByCreatedRange(long start, long end) {
    return findByPredicate(
        Predicate.AND(Predicate.GE("created", start), Predicate.LT("created", end)));
  }

  @Override
  public List<RcaInvestigationDTO> findByUpdatedRange(long start, long end) {
    return findByPredicate(
        Predicate.AND(Predicate.GE("updated", start), Predicate.LT("updated", end)));
  }

  @Override
  public List<RcaInvestigationDTO> findByAnomalyId(long id) {
    return findByPredicate(Predicate.EQ("anomalyId", id));
  }

  @Override
  public void registerDatabaseMetrics() {
    Gauge.builder("thirdeye_rca_investigations",
            memoizeWithExpiration(this::count, METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);
    LOG.info("Registered RCA investigation database metrics.");
  }
}
