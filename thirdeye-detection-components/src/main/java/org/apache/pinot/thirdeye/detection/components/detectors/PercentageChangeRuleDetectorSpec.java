package org.apache.pinot.thirdeye.detection.components.detectors;

import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;

public class PercentageChangeRuleDetectorSpec extends AbstractSpec {

  private double percentageChange = Double.NaN;
  private String offset = "wo1w";
  private String timezone = DEFAULT_TIMEZONE;
  private String pattern = "UP_OR_DOWN";
  private String weekStart = "WEDNESDAY";
  private String monitoringGranularity = MetricSlice.NATIVE_GRANULARITY
      .toAggregationGranularityString(); // use native granularity by default

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

  public String getTimezone() {
    return timezone;
  }

  public PercentageChangeRuleDetectorSpec setTimezone(final String timezone) {
    this.timezone = timezone;
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

  public String getMonitoringGranularity() {
    return monitoringGranularity;
  }

  public PercentageChangeRuleDetectorSpec setMonitoringGranularity(
      final String monitoringGranularity) {
    this.monitoringGranularity = monitoringGranularity;
    return this;
  }
}
