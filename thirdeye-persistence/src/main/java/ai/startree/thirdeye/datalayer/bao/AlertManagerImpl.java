/*
 * Copyright 2023 StarTree Inc
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

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class AlertManagerImpl extends AbstractManagerImpl<AlertDTO> implements
    AlertManager {

  @Inject
  public AlertManagerImpl(final GenericPojoDao genericPojoDao,
      final MetricRegistry metricRegistry) {
    super(AlertDTO.class, genericPojoDao);
    metricRegistry.register("activeAlertsCount", new CachedGauge<Long>(5, TimeUnit.MINUTES) {
      @Override
      public Long loadValue() {
        return countActive();
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

    final Long id = genericPojoDao.put(alertDTO);
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
