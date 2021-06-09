package org.apache.pinot.thirdeye.detection.v2.spec;

import java.util.Collections;
import java.util.List;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.detection.spec.AbstractSpec;

public class PercentageChangeRuleDetectorSpec extends AbstractSpec {

  private double percentageChange = Double.NaN;
  private String offset = "wo1w";
  private String timezone = DEFAULT_TIMEZONE;
  private String pattern = "UP_OR_DOWN";
  private String weekStart = "WEDNESDAY";
  private String timestamp = "timestamp";
  private String metric = "value";
  private List<String> dimensions = Collections.emptyList();
  private String monitoringGranularity = MetricSlice.NATIVE_GRANULARITY
      .toAggregationGranularityString(); // use native granularity by default

  public String getMonitoringGranularity() {
    return monitoringGranularity;
  }

  public void setMonitoringGranularity(String monitoringGranularity) {
    this.monitoringGranularity = monitoringGranularity;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getOffset() {
    return offset;
  }

  public void setOffset(String offset) {
    this.offset = offset;
  }

  public double getPercentageChange() {
    return percentageChange;
  }

  public void setPercentageChange(double percentageChange) {
    this.percentageChange = percentageChange;
  }

  public String getWeekStart() {
    return weekStart;
  }

  public void setWeekStart(String weekStart) {
    this.weekStart = weekStart;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public PercentageChangeRuleDetectorSpec setTimestamp(final String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public PercentageChangeRuleDetectorSpec setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public List<String> getDimensions() {
    return dimensions;
  }

  public PercentageChangeRuleDetectorSpec setDimensions(final List<String> dimensions) {
    this.dimensions = dimensions;
    return this;
  }
}
