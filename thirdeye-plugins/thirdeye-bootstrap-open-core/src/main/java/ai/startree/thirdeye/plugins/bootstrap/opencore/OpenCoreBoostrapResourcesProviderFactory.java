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
package ai.startree.thirdeye.plugins.bootstrap.opencore;

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProvider;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProviderFactory;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.google.auto.service.AutoService;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

@AutoService(Plugin.class)
public class OpenCoreBoostrapResourcesProviderFactory implements BootstrapResourcesProviderFactory {

  @Override
  public String name() {
    return "opencore";
  }

  @Override
  public @NonNull BootstrapResourcesProvider build() {
    return new OpenCoreBoostrapResourcesProvider();
  }
}
