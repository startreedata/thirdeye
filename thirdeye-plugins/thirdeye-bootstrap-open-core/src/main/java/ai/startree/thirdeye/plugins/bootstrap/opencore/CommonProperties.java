/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.bootstrap.opencore;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;

/**
 * The goal of this class is to maintain a list of description for commonly used properties.
 */
public class CommonProperties {

  // please maintain this list in alphabetical order
  public static final Map<String, String> COMMON_DESCRIPTIONS = ImmutableMap.<String, String>builder()
      .put("aggregationColumn", "The column to aggregate. Can be a derived metric.")
      .put("aggregationFunction", "The aggregation function to apply on the aggregationColumn. Eg AVG.")
      .put("aggregationParameter", "The second argument of the aggregationFunction. Eg for PERCENTILETDIGEST: 95.")
      .put("coldStartIgnore", "Ignore anomalies at the start of the dataset.")
      .put("completenessDelay", "The time for your data to be considered complete and ready for anomaly detection. In ISO-8601 format. Eg PT2H. See https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#completenessdelay.")
      .put("dataset", "The dataset to query.")
      .put("dataSource", "The Pinot datasource to use.")
      .put("dayHoursOfWeek", "A mapping of {DAY: [hours]}. Anomalies happening on these timeframes are ignored if timeOfWeekIgnore is true. Eg {\"FRIDAY\": [22, 23], \"SATURDAY\": [0, 1, 2]}")
      .put("daysOfWeek", "A list of days. Anomalies happening on these days are ignored if timeOfWeekIgnore is true. Eg [\"MONDAY\", \"SUNDAY\"].")
      .put("eventFilterAfterEventMargin", "Same as eventFilterBeforeEventMargin at the end of the event.")
      .put("eventFilterBeforeEventMargin", "A period in ISO-8601 format that corresponds to a period that is also impacted by the event. Eg if beforeEventMargin is P1D, if event happens on [Dec 24 0:00, Dec 25 0:00[, the label will be applied to anomalies happening on [Dec 23 0:00 and Dec 25 0:00[")
      .put("eventFilterIgnore", "Whether to ignore anomalies that happen during events.")
      .put("eventFilterLookaround", "Offset to apply on startTime and endTime to look around the timeframe. In ISO-8601 format. Eg P1D.")
      .put("eventFilterSqlFilter", "Sql filter to apply on the events. See https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/reference/operators/event-fetcher#sql-filter")
      .put("eventFilterTypes", "List of event types to filter by. Eg [\"HOLIDAY\", \"DEPLOYMENT\"]. [] or null means no filtering.")
      .put("guardrailMetric", "Metric to use as a threshold guardrail. Eg COUNT(*) and set guardrailMetricMin = 100 to ignore anomalies detected when there is less than 100 observations in the period.")
      .put("guardrailMetricIgnore", "Whether to ignore anomalies that don't meet the guardrail threshold.")
      .put("guardrailMetricMax", "Maximum threshold of guardrailMetric. If -1, no maximum threshold is applied.")
      .put("guardrailMetricMin", "Minimum threshold of the guardrail metric. If -1, no minimum threshold is applied.")
      .put("hoursOfDay", "A list of hours. Anomalies happening on these days are ignored if timeOfWeekIgnore is true. Eg [0,1,2,23]")
      .put("lookback", "Historical time period to use to train the model. In ISO-8601 format. Eg P21D.")
      .put("mergeMaxDuration", "Maximum gap between 2 anomalies for anomalies to be merged. In ISO-8601 format. Eg PT2H. To disable anomalies merging, set this value to P0D.")
      .put("mergeMaxGap", "Maximum duration of an anomaly merger. At merge time, if an anomaly merger would get bigger than this limit, the anomalies are not merged. In ISO-8601 format. Eg P7D.")
      .put("monitoringGranularity", "The period of aggregation of the timeseries. In ISO-8601 format. Eg PT1H.")
      .put("pattern", "Whether to detect an anomaly if it's a drop, a spike or any of the two.")
      .put("queryFilters", "Filters to apply when fetching data. Prefix with AND. Eg AND country='US'")
      .put("queryLimit", "Maximum number of timeseries point to fetch.")
      .put("rcaAggregationFunction", "The aggregation function to use for RCA. If the detection metric name is known to ThirdEye, this parameter is optional.")
      .put("rcaEventSqlFilter", "A Sql filter for RCA events. Only events that match the filter will be shown in the RCA related events tab. See https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#sqlfilter.")
      .put("rcaEventTypes", "A list of type to filter on for RCA. Only events that match such types will be shown in the RCA related events tab. See https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#types.")
      .put("rcaExcludedDimensions", "List of dimensions (columns in the dataset) to ignore in RCA drill-downs. If not set or empty, all dimensions of the table are used. rcaExcludedDimensions and rcaIncludedDimensions cannot be used at the same time.")
      .put("rcaIncludedDimensions", "List of the dimensions (columns in the dataset) to use in RCA drill-downs. If not set or empty, all dimensions of the table are used. See https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#dimensions.")
      .put("seasonalityPeriod", "Biggest seasonality period to learn. In ISO-8601 format. Eg P7D.")
      .put("sensitivity", "The sensitivity of the model. The smaller, the less anomaly are detected.")
      .put("thresholdFilterMax", "Filter threshold maximum. If -1, no maximum threshold is applied.")
      .put("thresholdFilterMin", "Filter threshold minimum. If -1, no minimum threshold is applied.")
      .put("thresholdIgnore", "Whether to ignore anomalies that don't meet the thresholdFilter min and max. Eg set thresholdFilterMin = 10 to ignore anomalies when the metric is smaller than 10. Can help ignore anomalies happening in low data regimes.")
      .put("timeColumn",
          "TimeColumn used to group by time. If set to AUTO (the default value), the Pinot primary time column is used.")
      .put("timeColumnFormat", "Required if timeColumn is not AUTO. See https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/reference/operators/data-fetcher#timeformat-strings.")
      .put("timezone", "Timezone used to group by time.")
      .put("timeOfWeekIgnore", "Whether to ignore time periods given in dayHoursOfWeek, daysOfWeek and hoursOfDay.")
      .build();

  // to complete and refactor once UI is ready
  private static final Map<String, List<Object>> COMMON_OPTIONS = ImmutableMap.<String, List<Object>>builder()
      .put("pattern", List.of("UP", "DOWN", "UP_OR_DOWN"))
      .build();

  public static void enrichCommonProperties(final List<AlertTemplateApi> templates) {
    for (final AlertTemplateApi template : templates) {
      final List<TemplatePropertyMetadata> properties = template.getProperties();
      if (properties == null) {
        continue;
      }
      for (final TemplatePropertyMetadata p : properties) {
        if (p.getDescription() == null) {
          p.setDescription(COMMON_DESCRIPTIONS.get(p.getName()));
        }
      }
      for (final TemplatePropertyMetadata p : properties) {
        if (p.getOptions() == null) {
          p.setOptions(COMMON_OPTIONS.get(p.getName()));
        }
      }
    }
  }
}
