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

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

  @SuppressWarnings("unused")
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

    return filtered.get();
  }
}
