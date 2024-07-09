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
package ai.startree.thirdeye.datalayer.entity;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractIndexEntity extends AbstractEntity {

  protected Long baseId;
  
  protected String namespace;

  public Long getBaseId() {
    return baseId;
  }

  public AbstractIndexEntity setBaseId(final Long baseId) {
    this.baseId = baseId;
    return this;
  }
  
  public String getNamespace() {
    return namespace;
  }
  
  public AbstractIndexEntity setNamespace(final @Nullable String namespace) {
    this.namespace = namespace;
    return this;
  }
}
