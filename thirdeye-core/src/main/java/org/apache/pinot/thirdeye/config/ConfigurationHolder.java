package org.apache.pinot.thirdeye.config;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.pinot.thirdeye.util.ConfigurationLoader.readConfig;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Map;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.rootcause.impl.RCAConfiguration;

public class ConfigurationHolder {

  private static final Map<Class, String> CONFIG_CLASS_MAP = ImmutableMap.<Class, String>builder()
      .put(ThirdEyeWorkerConfiguration.class, "detector.yml")
      .put(ThirdEyeSchedulerConfiguration.class, "scheduler.yaml")
      .put(RCAConfiguration.class, "rca.yml")
      .build();

  private final String path;

  public ConfigurationHolder(final String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public <T> T createConfigurationInstance(Class<T> clazz) {
    final String filename = CONFIG_CLASS_MAP.get(clazz);
    checkArgument(filename != null, String.format("Unknown config class: %s", clazz));
    final File file = new File(String.format("%s%s%s", getPath(), File.separator, filename));

    return readConfig(file, clazz);
  }
}
