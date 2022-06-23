package ai.startree.thirdeye.spi.bootstrap;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface BootstrapResourcesProviderFactory {

  String name();

  @NonNull BootstrapResourcesProvider build();
}
