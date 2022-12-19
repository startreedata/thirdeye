/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.auth;

import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import javax.annotation.Nullable;

public class ResourceIdentifier {

  static public final String DefaultNamespace = "default";

  public final String name;
  public final String namespace;
  public final EntityType entityType;

  public ResourceIdentifier(final String name, @Nullable final String namespace,
      final EntityType entityType) {
    this.name = name;
    this.namespace = namespace != null ? namespace : DefaultNamespace;
    this.entityType = entityType;
  }

  static public ResourceIdentifier fromDto(final AbstractDTO dto) {
    if (dto instanceof AlertDTO) {
      return fromAlertDto((AlertDTO) dto);
    }
    if (dto instanceof AlertTemplateDTO) {
      return fromAlertTemplateDto((AlertTemplateDTO) dto);
    }

    // TODO jackson: Add remaining resources.

    return fromUnspecified(dto.getId());
  }

  static public ResourceIdentifier fromAlertDto(final AlertDTO dto) {
    return new ResourceIdentifier(dto.getName(), dto.getNamespace(), EntityType.Alert);
  }

  static public ResourceIdentifier fromAlertTemplateDto(final AlertTemplateDTO dto) {
    return new ResourceIdentifier(dto.getName(), dto.getNamespace(), EntityType.AlertTemplate);
  }

  static public ResourceIdentifier fromUnspecified(final Long id) {
    return new ResourceIdentifier(
        (id != null) ? id.toString() : "none",
        ResourceIdentifier.DefaultNamespace,
        EntityType.Unspecified
    );
  }
}
