package org.apache.pinot.thirdeye.config;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.pinot.thirdeye.util.ConfigurationLoader.readConfig;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.rootcause.impl.RCAConfiguration;

public class ConfigurationHolder {

  /**
   * The files: coordinator.yaml and detector.yaml files are directly read by the dropwizard
   * framework itself and therefore skipped in this map.
   */
  private Map<Class, String> configClassMap = new HashMap<>(ImmutableMap.<Class, String>builder()
      .put(ThirdEyeSchedulerConfiguration.class, "scheduler.yaml")
      .put(CacheConfig.class, "data-sources/cache-config.yml")
      .put(RCAConfiguration.class, "rca.yml")
      .build());

  // root configuration path
  private final String path;

  public ConfigurationHolder(final String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public <T> T createConfigurationInstance(Class<T> clazz) {
    final String filename = configClassMap.get(clazz);
    checkArgument(filename != null, String.format("Unknown config class: %s", clazz));
    final File file = new File(String.format("%s%s%s", getPath(), File.separator, filename));

    if (file.exists()) {
      return readConfig(file, clazz);
    } else {
      try {
        return clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Map<Class, String> getConfigClassMap() {
    return configClassMap;
  }

  public ConfigurationHolder setConfigClassMap(
      final Map<Class, String> configClassMap) {
    this.configClassMap = configClassMap;
    return this;
  }
}
