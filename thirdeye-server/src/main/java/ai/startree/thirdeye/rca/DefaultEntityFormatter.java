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
package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.spi.api.RootCauseEntity;

/**
 * Default formatter that applies to any Entity. Provides minimal information and serves as
 * a fallback.
 */
public class DefaultEntityFormatter extends RootCauseEntityFormatter {
  public static final String TYPE_OTHER = "other";

  @Override
  public boolean applies(Entity entity) {
    return true;
  }

  @Override
  public RootCauseEntity format(Entity entity) {
    return makeRootCauseEntity(entity, TYPE_OTHER, entity.getUrn(), null);
  }
}
