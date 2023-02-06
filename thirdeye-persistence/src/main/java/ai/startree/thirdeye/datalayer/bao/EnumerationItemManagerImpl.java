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

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class EnumerationItemManagerImpl extends AbstractManagerImpl<EnumerationItemDTO>
    implements EnumerationItemManager {

  @Inject
  public EnumerationItemManagerImpl(final GenericPojoDao genericPojoDao) {
    super(EnumerationItemDTO.class, genericPojoDao);
  }

  private static boolean matches(final EnumerationItemDTO o1, final EnumerationItemDTO o2) {
    return Objects.equals(o1.getName(), o2.getName())
        && Objects.equals(o1.getParams(), o2.getParams());
  }

  private static AlertDTO toAlertDTO(final Long alertId) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(alertId);
    return alert;
  }

  @Override
  public EnumerationItemDTO findExistingOrCreate(final EnumerationItemDTO source) {
    requireNonNull(source.getName(), "enumeration item name does not exist!");
    final List<EnumerationItemDTO> byName = findByName(source.getName());

    final Optional<EnumerationItemDTO> filtered = optional(byName).orElse(emptyList()).stream()
        .filter(e -> matches(source, e))
        .findFirst();

    if (filtered.isEmpty()) {
      /* Create new */
      save(source);
      requireNonNull(source.getId(), "expecting a generated ID");
      return source;
    }

    final var existing = filtered.get();

    // source is always expected to have exactly 1 alert
    requireNonNull(source.getAlerts(), "expecting a valid alert id");
    checkState(source.getAlerts().size() == 1, "expecting exactly 1 alert");

    final Long alertId = source.getAlerts().get(0).getId();
    requireNonNull(alertId, "expecting a valid alert id");

    final boolean noUpdateRequired = optional(existing.getAlerts()).orElse(List.of()).stream()
        .anyMatch(alert -> alertId.equals(alert.getId()));

    if (noUpdateRequired) {
      return existing;
    }

    final List<AlertDTO> combined = combine(source.getAlerts(), existing.getAlerts());
    save(existing.setAlerts(combined));
    return existing;
  }

  private List<AlertDTO> combine(final List<AlertDTO> alerts, final List<AlertDTO> existing) {
    final Set<Long> alertIds = optional(alerts).orElse(List.of()).stream()
        .map(AlertDTO::getId)
        .collect(Collectors.toSet());

    optional(existing).orElse(List.of()).stream()
        .map(AlertDTO::getId)
        .forEach(alertIds::add);

    return alertIds.stream()
        .map(EnumerationItemManagerImpl::toAlertDTO)
        .collect(Collectors.toList());
  }
}
