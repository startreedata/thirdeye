/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.sql;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlDataset {

  @JsonProperty
  private String tableName;
  @JsonProperty
  private String timeColumn;
  @JsonProperty
  private final List<String> dimensions = new ArrayList<>();
  @JsonProperty
  private Map<String, MetricAggFunction> metrics;
  @JsonProperty
  private final String granularity = "1DAYS";
  @JsonProperty
  private final String timezone = Constants.DEFAULT_TIMEZONE_STRING;
  @JsonProperty
  private final String dataFile = "";
  @JsonProperty
  private final String timeFormat = "EPOCH";

  public String getTimeColumn() {
    return timeColumn;
  }

  public List<String> getDimensions() {
    return dimensions;
  }

  public Map<String, MetricAggFunction> getMetrics() {
    return metrics;
  }

  public String getGranularity() {
    return granularity;
  }

  public String getTimezone() {
    return timezone;
  }

  public String getTimeFormat() {
    return timeFormat;
  }

  public String getTableName() {
    return tableName;
  }

  public String getDataFile() {
    return dataFile;
  }

  @Override
  public String toString() {
    return "SqlDataset{" + "tableName='" + tableName + '\'' + ", timeColumn='" + timeColumn + '\''
        + ", dimensions="
        + dimensions + ", metrics=" + metrics + ", granularity='" + granularity + '\''
        + ", timezone='" + timezone
        + '\'' + ", timeFormat='" + timeFormat + '\'' + '}' + ", dataFile='" + dataFile + '\'';
  }
}
