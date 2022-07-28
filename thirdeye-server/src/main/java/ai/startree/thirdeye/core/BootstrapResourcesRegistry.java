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
package ai.startree.thirdeye.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProvider;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProviderFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class BootstrapResourcesRegistry {

  private final Map<String, BootstrapResourcesProviderFactory> factoryMap = new HashMap<>();

  private final Map<String, BootstrapResourcesProvider> cache = new HashMap<>();

  @Inject
  public BootstrapResourcesRegistry() {
  }

  public void addBootstrapResourcesProviderFactory(final BootstrapResourcesProviderFactory f) {
    checkArgument(f.name() != null, "name is null");
    checkState(!factoryMap.containsKey(f.name()),
        "Duplicate BootstrapResourcesProviderFactory: " + f.name());

    factoryMap.put(f.name(), f);
  }

  private @NonNull BootstrapResourcesProvider get(final String name) {
    return cache.computeIfAbsent(name, k -> loadBootstrapResourcesProvider(name));
  }

  private @NonNull BootstrapResourcesProvider loadBootstrapResourcesProvider(final String name) {
    final BootstrapResourcesProviderFactory bootstrapResourcesProviderFactory = factoryMap.get(name);
    checkArgument(bootstrapResourcesProviderFactory != null,
        "Unable to load bootstrapResourcesProviderFactory for: " + name);

    return bootstrapResourcesProviderFactory.build();
  }

  public List<AlertTemplateApi> getAlertTemplates() {
    List<AlertTemplateApi> allTemplates = new ArrayList<>();
    for (String name : factoryMap.keySet()) {
      final BootstrapResourcesProvider bootstrapResourcesProvider = get(name);
      allTemplates.addAll(bootstrapResourcesProvider.getAlertTemplates());
    }

    return allTemplates;
  }
}
