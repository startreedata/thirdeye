package org.apache.pinot.thirdeye.spi.datasource.macro;

import java.util.Map;
import org.joda.time.Interval;

public class MacroFunctionContext {

  private SqlExpressionBuilder sqlExpressionBuilder;
  private Interval detectionInterval;
  private Map<String, String> properties;

  public SqlExpressionBuilder getSqlExpressionBuilder() {
    return sqlExpressionBuilder;
  }

  public MacroFunctionContext setSqlExpressionBuilder(
      final SqlExpressionBuilder sqlExpressionBuilder) {
    this.sqlExpressionBuilder = sqlExpressionBuilder;
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
