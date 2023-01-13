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
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata.JsonType;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The goal of this class is to maintain a list of description for commonly used properties.
 */
public class CommonProperties {

  // please maintain this list in alphabetical order
  public static final List<TemplatePropertyMetadata> COMMON_PROPERTIES = ImmutableList.<TemplatePropertyMetadata>builder()
      .add(new TemplatePropertyMetadata().setName("aggregationColumn")
          .setDescription("The column to aggregate. Can be a derived metric."))
      .add(new TemplatePropertyMetadata().setName("aggregationFunction")
          .setDescription(
              "The aggregation function to apply on the aggregationColumn. Example: `AVG`."))
      .add(new TemplatePropertyMetadata().setName("aggregationParameter")
          .setDescription(
              "The second argument of the aggregationFunction. Example: for `PERCENTILETDIGEST`: `95`."))
      .add(new TemplatePropertyMetadata().setName("coldStartIgnore")
          .setDescription("Ignore anomalies at the start of the dataset."))
      .add(new TemplatePropertyMetadata().setName("completenessDelay")
          .setDescription(
              "The time for your data to be considered complete and ready for anomaly detection. In ISO-8601 format. Example: `PT2H`. [Learn more](https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#completenessdelay)."))
      .add(
          new TemplatePropertyMetadata().setName("dataset").setDescription("The dataset to query."))
      .add(new TemplatePropertyMetadata().setName("dataSource")
          .setDescription("The Pinot datasource to use."))
      .add(new TemplatePropertyMetadata().setName("dayHoursOfWeek")
          .setDescription(
              "A mapping of `{DAY: [hours]}`. Anomalies happening on these timeframes are ignored if timeOfWeekIgnore is true. Example: `{\"FRIDAY\": [22, 23], \"SATURDAY\": [0, 1, 2]}`"))
      .add(new TemplatePropertyMetadata().setName("daysOfWeek")
          .setDescription(
              "A list of days. Anomalies happening on these days are ignored if timeOfWeekIgnore is true. Example: `[\"MONDAY\", \"SUNDAY\"]`.")
          .setOptions(
              List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"))
          .setMultiselect(true))
      .add(new TemplatePropertyMetadata().setName("eventFilterAfterEventMargin")
          .setDescription("Same as eventFilterBeforeEventMargin at the end of the event."))
      .add(new TemplatePropertyMetadata().setName("eventFilterBeforeEventMargin")
          .setDescription(
              "A period in ISO-8601 format that corresponds to a period that is also impacted by the event. Example: if beforeEventMargin is `P1D`, if event happens on `[Dec 24 0:00, Dec 25 0:00[`, the label will be applied to anomalies happening on `[Dec 23 0:00 and Dec 25 0:00[`"))
      .add(new TemplatePropertyMetadata().setName("eventFilterIgnore")
          .setDescription("Whether to ignore anomalies that happen during events."))
      .add(new TemplatePropertyMetadata().setName("eventFilterLookaround")
          .setDescription(
              "Offset to apply on startTime and endTime to look around the timeframe. In ISO-8601 format. Example: `P1D`."))
      .add(new TemplatePropertyMetadata().setName("eventFilterSqlFilter")
          .setDescription(
              "Sql filter to apply on the events. [Learn more](https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/reference/operators/event-fetcher#sql-filter)"))
      .add(new TemplatePropertyMetadata().setName("eventFilterTypes")
          .setDescription(
              "List of event types to filter by. Example: `[\"HOLIDAY\", \"DEPLOYMENT\"]`. `[]` or `null` means no filtering."))
      .add(new TemplatePropertyMetadata().setName("guardrailMetric")
          .setDescription(
              "Metric to use as a threshold guardrail. Example: `COUNT(*)` and set `guardrailMetricMin = 100` to ignore anomalies detected when there is less than 100 observations in the period."))
      .add(new TemplatePropertyMetadata().setName("guardrailMetricIgnore")
          .setDescription("Whether to ignore anomalies that don't meet the guardrail threshold."))
      .add(new TemplatePropertyMetadata().setName("guardrailMetricMax")
          .setDescription(
              "Maximum threshold of guardrailMetric. If `-1`, no maximum threshold is applied."))
      .add(new TemplatePropertyMetadata().setName("guardrailMetricMin")
          .setDescription(
              "Minimum threshold of the guardrail metric. If `-1`, no minimum threshold is applied."))
      .add(new TemplatePropertyMetadata().setName("hoursOfDay")
          .setDescription(
              "A list of hours. Anomalies happening on these days are ignored if timeOfWeekIgnore is true. Example: `[0,1,2,23]`"))
      .add(new TemplatePropertyMetadata().setName("lookback")
          .setDescription(
              "Historical time period to use to train the model. In ISO-8601 format. Example: `P21D`."))
      .add(new TemplatePropertyMetadata().setName("mergeMaxDuration")
          .setDescription(
              "Maximum gap between 2 anomalies for anomalies to be merged. In ISO-8601 format. Example: `PT2H`. To disable anomalies merging, set this value to `P0D`."))
      .add(new TemplatePropertyMetadata().setName("mergeMaxGap")
          .setDescription(
              "Maximum duration of an anomaly merger. At merge time, if an anomaly merger would get bigger than this limit, the anomalies are not merged. In ISO-8601 format. Example: `P7D`."))
      .add(new TemplatePropertyMetadata().setName("monitoringGranularity")
          .setDescription(
              "The period of aggregation of the timeseries. In ISO-8601 format. Example: `PT1H`."))
      .add(new TemplatePropertyMetadata().setName("pattern")
          .setDescription("Whether to detect an anomaly if it's a drop, a spike or any of the two.")
          .setOptions(List.of("UP", "DOWN", "UP_OR_DOWN")))
      .add(new TemplatePropertyMetadata().setName("queryFilters")
          .setDescription(
              "Filters to apply when fetching data. Prefix with `AND`. Example: `AND country='US'`"))
      .add(new TemplatePropertyMetadata().setName("queryLimit")
          .setDescription("Maximum number of timeseries point to fetch."))
      .add(new TemplatePropertyMetadata().setName("rcaAggregationFunction")
          .setDescription(
              "The aggregation function to use for RCA. If the detection metric name is known to ThirdEye, this parameter is optional."))
      .add(new TemplatePropertyMetadata().setName("rcaEventSqlFilter")
          .setDescription(
              "A Sql filter for RCA events. Only events that match the filter will be shown in the RCA related events tab. [Learn more](https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#sqlfilter)."))
      .add(new TemplatePropertyMetadata().setName("rcaEventTypes")
          .setDescription(
              "A list of type to filter on for RCA. Only events that match such types will be shown in the RCA related events tab. [Learn more](https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#types)."))
      .add(new TemplatePropertyMetadata().setName("rcaExcludedDimensions")
          .setDescription(
              "List of dimensions (columns in the dataset) to ignore in RCA drill-downs. If not set or empty, all dimensions of the table are used. rcaExcludedDimensions and rcaIncludedDimensions cannot be used at the same time.")
          .setJsonType(JsonType.ARRAY))
      .add(new TemplatePropertyMetadata().setName("rcaIncludedDimensions")
          .setDescription(
              "List of the dimensions (columns in the dataset) to use in RCA drill-downs. If not set or empty, all dimensions of the table are used. [Learn more](https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/alert-configuration#dimensions).")
          .setJsonType(JsonType.ARRAY))
      .add(new TemplatePropertyMetadata().setName("seasonalityPeriod")
          .setDescription(
              "Biggest seasonality period to learn. In ISO-8601 format. Example: `P7D`."))
      .add(new TemplatePropertyMetadata().setName("sensitivity")
          .setDescription(
              "The sensitivity of the model. The smaller, the less anomaly are detected."))
      .add(new TemplatePropertyMetadata().setName("thresholdFilterMax")
          .setDescription("Filter threshold maximum. If `-1`, no maximum threshold is applied."))
      .add(new TemplatePropertyMetadata().setName("thresholdFilterMin")
          .setDescription("Filter threshold minimum. If `-1`, no minimum threshold is applied."))
      .add(new TemplatePropertyMetadata().setName("thresholdIgnore")
          .setDescription(
              "Whether to ignore anomalies that don't meet the thresholdFilter min and max. Example: set `thresholdFilterMin = 10` to ignore anomalies when the metric is smaller than 10. Can help ignore anomalies happening in low data regimes."))
      .add(new TemplatePropertyMetadata().setName("timeColumn")
          .setDescription(
              "TimeColumn used to group by time. If set to AUTO (the default value), the Pinot primary time column is used."))
      .add(new TemplatePropertyMetadata().setName("timeColumnFormat")
          .setDescription(
              "Required if timeColumn is not AUTO. [Learn more](https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/reference/operators/data-fetcher#timeformat-strings)."))
      .add(new TemplatePropertyMetadata().setName("timezone")
          .setDescription("Timezone used to group by time."))
      .add(new TemplatePropertyMetadata().setName("timeOfWeekIgnore")
          .setDescription(
              "Whether to ignore time periods given in dayHoursOfWeek, daysOfWeek and hoursOfDay."))
      .build();

  public static final Map<String, TemplatePropertyMetadata> NAME_TO_PROPERTY = COMMON_PROPERTIES.stream()
      .collect(Collectors.toMap(TemplatePropertyMetadata::getName, e -> e));

  public static void enrichCommonProperties(final List<AlertTemplateApi> templates) {
    for (final AlertTemplateApi template : templates) {
      final List<TemplatePropertyMetadata> properties = template.getProperties();
      if (properties == null) {
        continue;
      }
      for (final TemplatePropertyMetadata p : properties) {
        final TemplatePropertyMetadata commonProperty = NAME_TO_PROPERTY.get(p.getName());
        if (commonProperty == null) {
          continue;
        }
        // hand coded mapping because getting this exact behavior was not easy with MapStruct
        // behaviour: don't erase what's existing - don't try to map every field
        if (p.getDescription() == null) {
          p.setDescription(commonProperty.getDescription());
        }
        if (p.getOptions() == null) {
          p.setOptions(commonProperty.getOptions());
        }
        if (p.isMultiselect() == null) {
          p.setMultiselect(commonProperty.isMultiselect());
        }
        if (p.getJsonType() == null) {
          p.setJsonType(commonProperty.getJsonType());
        }
      }
    }
  }
}
