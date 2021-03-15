package org.apache.pinot.thirdeye.config;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.rootcause.impl.RCAConfiguration;

public class ConfigurationHolder {

  private static final Map<String, Class> configClassMap = ImmutableMap.<String, Class>builder()
      .put("detector.yml", ThirdEyeWorkerConfiguration.class)
      .put("rca.yml", RCAConfiguration.class)
      .build();

  private String path = "config";

  public String getPath() {
    return path;
  }

  public ConfigurationHolder setPath(final String path) {
    this.path = path;
    return this;
  }
}
