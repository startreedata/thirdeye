/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import ai.startree.thirdeye.spi.detection.BaselineParsingUtils;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import ai.startree.thirdeye.spi.rootcause.timeseries.Baseline;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Absolute change anomaly filter. Check if the anomaly's absolute change compared to baseline is
 * above the threshold.
 * If not, filters the anomaly.
 */
public class AbsoluteChangeRuleAnomalyFilter implements
    AnomalyFilter<AbsoluteChangeRuleAnomalyFilterSpec> {

  private double threshold;
  private InputDataFetcher dataFetcher;
  private Baseline baseline;
  private Pattern pattern;

  @Override
  public void init(AbsoluteChangeRuleAnomalyFilterSpec spec) {
    this.pattern = Pattern.valueOf(spec.getPattern().toUpperCase());
    // customize baseline offset
    if (StringUtils.isNotBlank(spec.getOffset())) {
      this.baseline = BaselineParsingUtils.parseOffset(spec.getOffset(), spec.getTimezone());
    }
    this.threshold = spec.getThreshold();
  }

  @Override
  public void init(AbsoluteChangeRuleAnomalyFilterSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    MetricEntity me = MetricEntity.fromURN(anomaly.getMetricUrn());
    List<MetricSlice> slices = new ArrayList<>();
    MetricSlice currentSlice =
        MetricSlice.from(me.getId(), anomaly.getStartTime(), anomaly.getEndTime(), me.getFilters());

    // customize baseline offset
    if (baseline != null) {
      slices.addAll(this.baseline.scatter(currentSlice));
    }

    Map<MetricSlice, DataFrame> aggregates =
        this.dataFetcher.fetchData(new InputDataSpec().withAggregateSlices(slices)).getAggregates();

    double currentValue = anomaly.getAvgCurrentVal();
    double baselineValue =
        baseline == null ? anomaly.getAvgBaselineVal()
            : this.baseline.gather(currentSlice, aggregates).getDouble(
                DataFrame.COL_VALUE, 0);
    // if inconsistent with up/down, filter the anomaly
    if (!pattern.equals(Pattern.UP_OR_DOWN) && (currentValue < baselineValue && pattern
        .equals(Pattern.UP)) || (
        currentValue > baselineValue && pattern.equals(Pattern.DOWN))) {
      return false;
    }
    return Math.abs(currentValue - baselineValue) >= this.threshold;
  }
}
