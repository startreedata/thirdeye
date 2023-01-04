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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.datalayer.dao.SubEntities;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import java.util.Objects;

public class ResourceIdentifier {

  static public final String DEFAULT_NAME = "0";
  static public final String DEFAULT_NAMESPACE = "default";
  static public final String DEFAULT_ENTITY_TYPE = "RESOURCE";

  public final String name;
  public final String namespace;
  public final String entityType;

  public ResourceIdentifier(final String name, final String namespace, final String entityType) {
    this.name = name;
    this.namespace = namespace;
    this.entityType = entityType;
  }

  static public <T extends AbstractDTO> ResourceIdentifier from(final T dto) {
    return new ResourceIdentifier(
        optional(dto.getId()).map(Objects::toString).orElse(DEFAULT_NAME),
        optional(dto.getNamespace()).orElse(DEFAULT_NAMESPACE),
        optional(SubEntities.BEAN_TYPE_MAP.get(dto.getClass()))
            .map(Objects::toString).orElse(DEFAULT_ENTITY_TYPE)
    );
  }
}
