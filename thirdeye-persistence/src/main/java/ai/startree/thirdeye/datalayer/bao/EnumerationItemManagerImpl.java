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
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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

  public static boolean matches(final EnumerationItemDTO o1, final EnumerationItemDTO o2) {
    return Objects.equals(o1.getName(), o2.getName())
        && Objects.equals(o1.getParams(), o2.getParams());
  }

  public static AlertDTO toAlertDTO(final Long alertId) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(alertId);
    return alert;
  }

  public static EnumerationItemDTO eiRef(final long id) {
    final EnumerationItemDTO enumerationItemDTO = new EnumerationItemDTO();
    enumerationItemDTO.setId(id);
    return enumerationItemDTO;
  }

  public static Map<String, Object> key(final EnumerationItemDTO source,
      final List<String> idKeys) {
    final var p = source.getParams();
    return idKeys.stream()
        .filter(p::containsKey)
        .collect(toMap(Function.identity(), p::get));
  }

  @Override
  public EnumerationItemDTO findExistingOrCreate(final EnumerationItemDTO source,
      final List<String> idKeys) {
    requireNonNull(source.getName(), "enumeration item name does not exist!");
    requireNonNull(source.getAlert(), "enumeration item needs a source alert!");

    final Long sourceAlertId = source.getAlert().getId();
    requireNonNull(sourceAlertId, "enumeration item needs a source alert with a valid id!");

    /*
     * If idKeys are provided, try to find an existing EnumerationItem with the same idKeys or
     * create. Either way, skip the rest of the logic including migration
     */
    if (idKeys != null && !idKeys.isEmpty()) {
      final EnumerationItemDTO existing = findUsingIdKeys(source, idKeys);
      if (existing != null) {
        if (!existing.getParams().equals(source.getParams()) ||
            !existing.getName().equals(source.getName())) {
          /*
           * Overwrite existing params with new params for the same key. The alert is the
           * source of truth.
           */
          save(existing
              .setParams(source.getParams())
              .setName(source.getName()));
        }
        return existing;
      }

      /* Create new */
      save(source);
      requireNonNull(source.getId(), "expecting a generated ID");
      return source;
    }

    /*
     * If there exists an EnumerationItem with the same name, check if it has the same params.
     */
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

  public EnumerationItemDTO findUsingIdKeys(final EnumerationItemDTO source,
      final List<String> idKeys) {
    final DaoFilter daoFilter = new DaoFilter()
        .setPredicate(Predicate.EQ("alertId", source.getAlert().getId()));
    final var sourceKey = key(source, idKeys);
    final List<EnumerationItemDTO> filtered = filter(daoFilter).stream()
        .filter(e -> sourceKey.equals(key(e, idKeys)))
        .collect(toList());

    checkState(filtered.size() <= 1,
        "Found multiple EnumerationItems for: %s ids: %s",
        source,
        filtered.stream().map(EnumerationItemDTO::getId).collect(toList()));

    return filtered.stream().findFirst().orElse(null);
  }

  @Override
  public void migrate(final EnumerationItemDTO from, final EnumerationItemDTO to) {
    requireNonNull(from.getId(), "expecting a generated ID");
    requireNonNull(to.getId(), "expecting a generated ID");
    requireNonNull(to.getAlert(), "expecting a valid alert");

    final Long toId = to.getId();
    final Long alertId = to.getAlert().getId();

    LOG.info("Migrating enumeration item {} to {} for alert {}", from.getId(), toId, alertId);

    /* Migrate anomalies */
    final var filter = new AnomalyFilter()
        .setEnumerationItemId(from.getId())
        .setAlertId(alertId);

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
            .anyMatch(aa -> from.getId().equals(aa.getEnumerationItem().getId())
                && alertId.equals(aa.getAlert().getId()))
        )
        .forEach(sg -> {
          sg.getAlertAssociations().stream()
              .filter(aa -> aa.getEnumerationItem() != null)
              .filter(aa -> from.getId().equals(aa.getEnumerationItem().getId())
                  && alertId.equals(aa.getAlert().getId()))
              .forEach(aa -> aa.setEnumerationItem(eiRef(toId)));
          subscriptionGroupManager.update(sg);
        });
  }
}
