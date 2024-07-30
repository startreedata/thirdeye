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
package ai.startree.thirdeye.spi.auth;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import org.apache.commons.lang3.StringUtils;

public class ResourceIdentifier {

  public static final String DEFAULT_NAME = "0";
  public static final String DEFAULT_NAMESPACE = "default";
  public static final String DEFAULT_ENTITY_TYPE = "RESOURCE";

  public static final ResourceIdentifier NULL_IDENTIFIER = ResourceIdentifier.from(DEFAULT_NAME, DEFAULT_NAMESPACE, DEFAULT_ENTITY_TYPE);

  private final String name;
  private final String namespace;
  private final String entityType;

  private ResourceIdentifier(final String name, final String namespace, final String entityType) {
    this.name = name;
    this.namespace = namespace;
    this.entityType = entityType;
  }

  public static ResourceIdentifier from(final String name, final String namespace,
      final String entityType) {
    return new ResourceIdentifier(
        optional(name).filter(StringUtils::isNotEmpty).orElse(DEFAULT_NAME),
        optional(namespace).filter(StringUtils::isNotEmpty).orElse(DEFAULT_NAMESPACE),
        optional(entityType).filter(StringUtils::isNotEmpty).orElse(DEFAULT_ENTITY_TYPE)
    );
  }

  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getEntityType() {
    return entityType;
  }
}
