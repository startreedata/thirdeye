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
package ai.startree.thirdeye.plugins.bootstrap.opencore;

import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProvider;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProviderFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

public class OpenCoreBoostrapResourcesProviderFactory implements BootstrapResourcesProviderFactory {

  @Override
  public String name() {
    return "open-core";
  }

  @Override
  public @NonNull BootstrapResourcesProvider build() {
    return new OpenCoreBoostrapResourcesProvider();
  }
}
