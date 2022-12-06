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
package ai.startree.thirdeye.authorization;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;

public class ResourceIdentifier {

  static public final String DefaultNamespace = "default";

  public final String name;
  public final String namespace;
  public final String entityType;

  public ResourceIdentifier(String name, String namespace, String entityType) {
    this.name = name;
    this.namespace = namespace;
    this.entityType = entityType;
  }

  static public ResourceIdentifier fromDto(AbstractDTO dto) {
    if (dto instanceof AlertDTO) {
      return new ResourceIdentifier(
          ((AlertDTO) dto).getName(),
          // TODO: Add a namespace field for alerts.
          ResourceIdentifier.DefaultNamespace,
          "alert"
      );
    }

    // TODO: Add remaining resources.

    return new ResourceIdentifier(
        dto.getId().toString(),
        ResourceIdentifier.DefaultNamespace,
        "unspecified"
    );
  }
}
