package ai.startree.thirdeye.bootstrap;

import static com.google.common.base.Preconditions.checkArgument;

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

  public void addBootstrapResourcesProviderFactory(final BootstrapResourcesProviderFactory factory) {
    checkArgument(factory.name() != null, "name is null");
    factoryMap.put(factory.name(), factory);
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
