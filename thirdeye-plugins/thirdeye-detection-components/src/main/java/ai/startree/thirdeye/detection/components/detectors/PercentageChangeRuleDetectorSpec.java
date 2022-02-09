package ai.startree.thirdeye.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class PercentageChangeRuleDetectorSpec extends AbstractSpec {

  private double percentageChange = Double.NaN;
  private String offset = "wo1w";
  private String pattern = "UP_OR_DOWN";
  private String weekStart = "WEDNESDAY";

  public double getPercentageChange() {
    return percentageChange;
  }

  public PercentageChangeRuleDetectorSpec setPercentageChange(final double percentageChange) {
    this.percentageChange = percentageChange;
    return this;
  }

  public String getOffset() {
    return offset;
  }

  public PercentageChangeRuleDetectorSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public PercentageChangeRuleDetectorSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }

  public String getWeekStart() {
    return weekStart;
  }

  public PercentageChangeRuleDetectorSpec setWeekStart(final String weekStart) {
    this.weekStart = weekStart;
    return this;
  }
}
