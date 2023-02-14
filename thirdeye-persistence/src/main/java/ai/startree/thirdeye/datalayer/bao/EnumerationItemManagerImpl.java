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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EnumerationItemManagerImpl extends AbstractManagerImpl<EnumerationItemDTO>
    implements EnumerationItemManager {

  private static final Logger LOG = LoggerFactory.getLogger(EnumerationItemManagerImpl.class);

  /* TODO Suvodeep Remove mega Hack. This is an antipattern. Ideally, persistence classes shouldn't
   *   depend on other persistence services */
  @Deprecated
  private final AnomalyManager anomalyManager;
  @Deprecated
  private final SubscriptionGroupManager subscriptionGroupManager;

  @Inject
  public EnumerationItemManagerImpl(final GenericPojoDao genericPojoDao,
      final AnomalyManager anomalyManager,
      final SubscriptionGroupManager subscriptionGroupManager) {
    super(EnumerationItemDTO.class, genericPojoDao);
    this.anomalyManager = anomalyManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
  }

  private static boolean matches(final EnumerationItemDTO o1, final EnumerationItemDTO o2) {
    return Objects.equals(o1.getName(), o2.getName())
        && Objects.equals(o1.getParams(), o2.getParams());
  }

  static AlertDTO toAlertDTO(final Long alertId) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(alertId);
    return alert;
  }

  private static EnumerationItemDTO eiRef(final long id) {
    final EnumerationItemDTO enumerationItemDTO = new EnumerationItemDTO();
    enumerationItemDTO.setId(id);
    return enumerationItemDTO;
  }

  @Override
  public EnumerationItemDTO findExistingOrCreate(final EnumerationItemDTO source) {
    requireNonNull(source.getName(), "enumeration item name does not exist!");
    requireNonNull(source.getAlert(), "enumeration item needs a source alert!");

    final Long sourceAlertId = source.getAlert().getId();
    requireNonNull(sourceAlertId, "enumeration item needs a source alert with a valid id!");

    final List<EnumerationItemDTO> byName = findByName(source.getName());
    final List<EnumerationItemDTO> matching = optional(byName).orElse(emptyList()).stream()
        .filter(e -> matches(source, e))
        .collect(toList());

    /* If there exists an EnumerationItem with a populated alert, return and no need to migrate */
    final List<EnumerationItemDTO> withAlert = matching.stream()
        .filter(ei -> ei.getAlert() != null)
        .filter(ei -> sourceAlertId.equals(ei.getAlert().getId()))
        .collect(toList());

    if (withAlert.size() > 0) {
      if (withAlert.size() > 1) {
        final List<Long> ids = withAlert.stream()
            .map(EnumerationItemDTO::getId)
            .collect(toList());
        LOG.error("Found more than one EnumerationItem with alert for name: {} ids: {}",
            source.getName(),
            ids);
      }
      return withAlert.get(0);
    }

    /* Create new */
    save(source);
    requireNonNull(source.getId(), "expecting a generated ID");

    /* Find enumeration item candidate which don't have an alert field set.
     * These are legacy enumeration items which need to be migrated to the new alert field
     **/
    matching.stream()
        .filter(ei -> ei.getAlert() == null)
        .forEach(ei -> migrate(ei, source));

    return source;
  }

  private void migrate(final EnumerationItemDTO from, final EnumerationItemDTO to) {
    requireNonNull(from.getId(), "expecting a generated ID");
    requireNonNull(to.getId(), "expecting a generated ID");
    requireNonNull(to.getAlert(), "expecting a valid alert");

    final Long toId = to.getId();
    final Long alertId = to.getAlert().getId();

    LOG.info("Migrating enumeration item {} to {} for alert {}", from.getId(), toId, alertId);

    /* Migrate anomalies */
    final var filter = new AnomalyFilter().setEnumerationItemId(from.getId());
    anomalyManager.filter(filter).stream()
        .filter(Objects::nonNull)
        .map(a -> a.setEnumerationItem(eiRef(toId)))
        .forEach(anomalyManager::update);

    /* Migrate subscription groups */
    subscriptionGroupManager.findAll().stream()
        .filter(Objects::nonNull)
        .filter(sg -> sg.getAlertAssociations() != null)
        .filter(sg -> sg.getAlertAssociations().stream()
            .filter(Objects::nonNull)
            .filter(aa -> aa.getEnumerationItem() != null)
            .anyMatch(aa -> from.getId().equals(aa.getEnumerationItem().getId()))
        )
        .forEach(sg -> {
          sg.getAlertAssociations().stream()
              .filter(aa -> aa.getEnumerationItem() != null)
              .filter(aa -> from.getId().equals(aa.getEnumerationItem().getId()))
              .forEach(aa -> aa.setEnumerationItem(eiRef(toId)));
          subscriptionGroupManager.update(sg);
        });
  }
}
