package org.apache.pinot.thirdeye.spi.datasource.macro;

import java.util.Map;
import org.joda.time.Interval;

public class MacroFunctionContext {

  private MacroManager macroManager;
  private Interval detectionInterval;
  private Map<String, String> properties;

  public MacroManager getMacroManager() {
    return macroManager;
  }

  public MacroFunctionContext setMacroManager(
      final MacroManager macroManager) {
    this.macroManager = macroManager;
    return this;
  }

  public Interval getDetectionInterval() {
    return detectionInterval;
  }

  public MacroFunctionContext setDetectionInterval(final Interval detectionInterval) {
    this.detectionInterval = detectionInterval;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public MacroFunctionContext setProperties(
      final Map<String, String> properties) {
    this.properties = properties;
    return this;
  }
}
