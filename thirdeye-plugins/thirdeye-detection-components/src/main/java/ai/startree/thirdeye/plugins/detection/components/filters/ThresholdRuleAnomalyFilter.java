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
package ai.startree.thirdeye.plugins.detection.components.filters;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * Aggregate Threshold Filter
 *
 * This threshold rule filter stage filters the anomalies if either the min or max thresholds do not
 * pass. filters the anomalies if either the min or max thresholds do not satisfied.
 */
public class ThresholdRuleAnomalyFilter implements AnomalyFilter<ThresholdRuleFilterSpec> {

  private double minValueHourly;
  private double maxValueHourly;
  private double minValueDaily;
  private double maxValueDaily;
  private double maxValue;
  private double minValue;
  private InputDataFetcher dataFetcher;

  @Override
  public void init(ThresholdRuleFilterSpec spec) {
    this.minValueHourly = spec.getMinValueHourly();
    this.maxValueHourly = spec.getMaxValueHourly();
    this.minValueDaily = spec.getMinValueDaily();
    this.maxValueDaily = spec.getMaxValueDaily();
    this.maxValue = spec.getMaxValue();
    this.minValue = spec.getMinValue();
  }

  @Override
  public void init(ThresholdRuleFilterSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    MetricConfigDTO metric = dataFetcher
        .fetchData(new InputDataSpec().withMetricIds(Collections.singleton(-1L)))
        .getMetrics()
        .get(-1);
    double currentValue = anomaly.getAvgCurrentVal();

    Interval anomalyInterval = new Interval(anomaly.getStartTime(), anomaly.getEndTime(), DateTimeZone.UTC);

    // apply multiplier if the metric is aggregated by SUM or COUNT
    double hourlyMultiplier = SpiUtils.isAggCumulative(metric) ?
        (TimeUnit.HOURS.toMillis(1) / (double) anomalyInterval.toDurationMillis()) : 1.0;
    double dailyMultiplier = SpiUtils.isAggCumulative(metric) ?
        (TimeUnit.DAYS.toMillis(1) / (double) anomalyInterval.toDurationMillis()) : 1.0;

    if (!Double.isNaN(this.minValue) && currentValue < this.minValue
        || !Double.isNaN(this.maxValue) && currentValue > this.maxValue) {
      return false;
    }
    if (!Double.isNaN(this.minValueHourly)
        && currentValue * hourlyMultiplier < this.minValueHourly) {
      return false;
    }
    if (!Double.isNaN(this.maxValueHourly)
        && currentValue * hourlyMultiplier > this.maxValueHourly) {
      return false;
    }
    if (!Double.isNaN(this.minValueDaily) && currentValue * dailyMultiplier < this.minValueDaily) {
      return false;
    }
    return Double.isNaN(this.maxValueDaily) || !(currentValue * dailyMultiplier
        > this.maxValueDaily);
  }
}
