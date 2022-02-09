package ai.startree.thirdeye.detection.components.triggers;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class ConsoleOutputTriggerSpec extends AbstractSpec {
  private String format = "Got trigger event = %s";

  public String getFormat() {
    return format;
  }

  public ConsoleOutputTriggerSpec setFormat(final String format) {
    this.format = format;
    return this;
  }
}
