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
package ai.startree.thirdeye.datalayer.core;

import static ai.startree.thirdeye.spi.util.SpiUtils.alertRef;
import static ai.startree.thirdeye.spi.util.SpiUtils.enumerationItemRef;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.EnumerationItemFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class EnumerationItemMaintainer {

  private static final Logger LOG = LoggerFactory.getLogger(EnumerationItemMaintainer.class);

  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;
  private final SubscriptionGroupManager subscriptionGroupManager;

  @Inject
  public EnumerationItemMaintainer(final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager,
      final SubscriptionGroupManager subscriptionGroupManager) {
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
  }

  public List<EnumerationItemDTO> sync(
      final List<EnumerationItemDTO> enumerationItems,
      final List<String> idKeys,
      final Long alertId) {
    // find existing enumerationItems
    // namespace filter is not necessary because we search by alert id - todo add namesapce to avoid future errors?
    final List<EnumerationItemDTO> existingItems = enumerationItemManager.filter(
        new EnumerationItemFilter().setAlertId(alertId));

    // update enumerationItems
    final List<EnumerationItemDTO> syncedItems = enumerationItems.stream()
        .map(source -> source.setAlert(alertRef(alertId)))
        // fixme cyril add a namespace to all enumeration items here
        .map(source -> findExistingOrCreate(source, idKeys, existingItems))
        .collect(toList());

    // delete existing enumerationItems that are not used anymore
    final Set<Long> syncedItemsIds = syncedItems.stream()
        .map(EnumerationItemDTO::getId)
        .collect(toSet());
    existingItems.stream()
        .filter(ei -> !syncedItemsIds.contains(ei.getId()))
        .forEach(this::delete);
    
    return syncedItems;
  }


  public void delete(final EnumerationItemDTO dto) {
    requireNonNull(dto.getId(), "EnumerationItemDTO.id cannot be null for deletion");
    String eiString;
    try {
      eiString = ThirdEyeSerialization
          .getObjectMapper()
          .writeValueAsString(dto);
    } catch (final Exception e) {
      eiString = dto.toString();
    }
    LOG.warn("Deleting enumeration item {} json: {}",
        dto.getId(),
        eiString);

    disassociateFromSubscriptionGroups(dto.getId());
    deleteAssociatedAnomalies(dto.getId());

    int success = enumerationItemManager.delete(dto);
    if (success == 0) {
      LOG.error("Failed to delete enumeration item {} json: {}", dto.getId(),
          eiString);
    }
  }

  private EnumerationItemDTO findExistingOrCreate(final EnumerationItemDTO source,
      final List<String> idKeys,
      final List<EnumerationItemDTO> existingEnumerationItems) {
    requireNonNull(source.getName(), "enumeration item name does not exist!");
    requireNonNull(source.getAlert(), "enumeration item needs a source alert!");

    final Long sourceAlertId = source.getAlert().getId();
    requireNonNull(sourceAlertId, "enumeration item needs a source alert with a valid id!");

    /*
     * If idKeys are provided, try to find an existing EnumerationItem with the same idKeys or
     * create. Either way, skip the rest of the logic including migration
     */
    if (idKeys != null && !idKeys.isEmpty()) {
      // fixme cyril authz look by namespace
      final EnumerationItemDTO existing = findUsingIdKeys(source, idKeys, existingEnumerationItems);
      if (existing != null) {
        updateExistingIfReqd(existing, source);
        return existing;
      } else {
        /* Create new */
        enumerationItemManager.save(source);
        requireNonNull(source.getId(), "expecting a generated ID");
        return source; 
      }
    }

    /*
     * If there exists an EnumerationItem with the same name, check if it has the same params.
     */
    // fixme cyril authz find by add namespace filter
    final List<EnumerationItemDTO> byName = enumerationItemManager.findByName(source.getName());
    final List<EnumerationItemDTO> matching = optional(byName).orElse(emptyList()).stream()
        .filter(e -> matches(source, e))
        .toList();

    /* If there exists an EnumerationItem with a populated alert, return and no need to migrate */
    final List<EnumerationItemDTO> withAlert = matching.stream()
        .filter(ei -> ei.getAlert() != null)
        .filter(ei -> sourceAlertId.equals(ei.getAlert().getId()))
        .toList();

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
    enumerationItemManager.save(source);
    requireNonNull(source.getId(), "expecting a generated ID");

    return source;
  }

  private void updateExistingIfReqd(final EnumerationItemDTO existing,
      final EnumerationItemDTO source) {
    if (!existing.getParams().equals(source.getParams()) ||
        !existing.getName().equals(source.getName()) ||
        !Objects.equals(existing.getAuth(), source.getAuth()) // auth can be null
    ) {
      /*
       * Overwrite existing params with new params for the same key. The alert is the
       * source of truth.
       */
      final EnumerationItemDTO updated = existing
          .setParams(source.getParams())
          .setName(source.getName());

      updated.setAuth(source.getAuth());
      enumerationItemManager.save(updated);
    }
  }

  private EnumerationItemDTO findUsingIdKeys(final EnumerationItemDTO source,
      final List<String> idKeys,
      final List<EnumerationItemDTO> existingEnumerationItems) {
    final Map<String, Object> sourceKey = key(source, idKeys);
    final List<EnumerationItemDTO> filtered = existingEnumerationItems.stream()
        .filter(e -> sourceKey.equals(key(e, idKeys)))
        .toList();

    if (filtered.size() > 1) {
      LOG.warn("Found more than one EnumerationItem for: {} ids: {}. Attempting to fix..",
          source,
          filtered.stream().map(EnumerationItemDTO::getId).collect(toList()));
      return handleConflicts(source, filtered);
    }
    return filtered.stream().findFirst().orElse(null);
  }

  /**
   * These are enumeration items that have the same idKeys. In this case, we find the best candidate
   * to keep and delete the rest. If a match isn't found, we keep the first one and delete the rest.
   *
   * @param source enumeration item to match with
   * @param eiList list of conflicting enumeration items that have the same idKeys
   */
  private EnumerationItemDTO handleConflicts(final EnumerationItemDTO source,
      final List<EnumerationItemDTO> eiList) {
    // find the one with same params
    final var matching = findCandidate(source, eiList);

    // Migrate subscription groups to matching candidate
    eiList.stream()
        .filter(e -> !e.getId().equals(matching.getId()))
        .forEach(e -> migrateSubscriptionGroups(
            e.getId(),
            matching.getId(),
            source.getAlert().getId()));

    // remove the rest
    eiList.stream()
        .filter(e -> !e.getId().equals(matching.getId()))
        .forEach(this::delete);

    return matching;
  }

  private void migrateSubscriptionGroups(final Long fromId, final Long toId, final Long alertId) {
    subscriptionGroupManager.findAll().stream()
        .filter(Objects::nonNull)
        .filter(sg -> sg.getAlertAssociations() != null)
        .filter(sg -> sg.getAlertAssociations().stream()
            .filter(Objects::nonNull)
            .filter(aa -> aa.getEnumerationItem() != null)
            .anyMatch(aa -> fromId.equals(aa.getEnumerationItem().getId())
                && alertId.equals(aa.getAlert().getId()))
        )
        .forEach(sg -> {
          sg.getAlertAssociations().stream()
              .filter(aa -> aa.getEnumerationItem() != null)
              .filter(aa -> fromId.equals(aa.getEnumerationItem().getId())
                  && alertId.equals(aa.getAlert().getId()))
              .forEach(aa -> aa.setEnumerationItem(enumerationItemRef(toId)));
          subscriptionGroupManager.update(sg);
        });
  }

  private void deleteAssociatedAnomalies(final Long enumerationItemId) {
    // todo cyril authz filter 
    final List<AnomalyDTO> anomalies = anomalyManager.filter(
        new AnomalyFilter().setEnumerationItemId(enumerationItemId));
    anomalies.forEach(anomalyManager::delete);
  }

  // todo cyril authz filter
  private void disassociateFromSubscriptionGroups(final Long enumerationItemId) {
    final List<SubscriptionGroupDTO> allSubscriptionGroups = subscriptionGroupManager.findAll();

    final List<SubscriptionGroupDTO> updated = new ArrayList<>();
    for (final SubscriptionGroupDTO sg : allSubscriptionGroups) {
      optional(sg.getAlertAssociations())
          .map(aas -> aas.removeIf(aa ->
              aa.getEnumerationItem() != null &&
                  enumerationItemId.equals(aa.getEnumerationItem().getId())))
          .filter(b -> b)
          .ifPresent(b -> updated.add(sg));
    }
    subscriptionGroupManager.update(updated);
  }

  private static boolean matches(final EnumerationItemDTO o1, final EnumerationItemDTO o2) {
    return Objects.equals(o1.getName(), o2.getName())
        && Objects.equals(o1.getParams(), o2.getParams());
  }

  private static Map<String, Object> key(final EnumerationItemDTO source,
      final List<String> idKeys) {
    final Map<String, Object> p = source.getParams();
    return idKeys.stream()
        .filter(p::containsKey)
        .collect(toMap(Function.identity(), p::get));
  }

  private static EnumerationItemDTO findCandidate(final EnumerationItemDTO source,
      final List<EnumerationItemDTO> eiList) {
    // Find exact match
    Optional<EnumerationItemDTO> candidate = eiList.stream()
        .filter(e -> matches(source, e))
        .findFirst();
    if (candidate.isPresent()) {
      return candidate.get();
    }

    // find one with same params
    candidate = eiList.stream()
        .filter(e -> e.getParams().equals(source.getParams()))
        .findFirst();

    // Just return the first one
    return candidate.orElse(eiList.get(0));
  }
}
