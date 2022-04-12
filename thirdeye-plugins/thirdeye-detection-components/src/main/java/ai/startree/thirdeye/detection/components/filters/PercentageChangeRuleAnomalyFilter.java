/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import ai.startree.thirdeye.spi.detection.BaselineParsingUtils;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.spi.rootcause.timeseries.Baseline;
import com.google.common.collect.ArrayListMultimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Percentage change anomaly filter. Check if the anomaly's percentage change compared to baseline
 * is above the threshold.
 * If not, filters the anomaly.
 */
public class PercentageChangeRuleAnomalyFilter implements
    AnomalyFilter<PercentageChangeRuleAnomalyFilterSpec> {

  private static final Logger LOG = LoggerFactory
      .getLogger(PercentageChangeRuleAnomalyFilter.class);
  private double threshold;
  private double upThreshold;
  private double downThreshold;
  private InputDataFetcher dataFetcher;
  private Baseline baseline;
  private Pattern pattern;

  @Override
  public void init(PercentageChangeRuleAnomalyFilterSpec spec) {
    this.pattern = Pattern.valueOf(spec.getPattern().toUpperCase());
    // customize baseline offset
    if (StringUtils.isNotBlank(spec.getOffset())) {
      this.baseline = BaselineParsingUtils.parseOffset(spec.getOffset(), spec.getTimezone());
    }
    this.threshold = spec.getThreshold();
    this.upThreshold = spec.getUpThreshold();
    this.downThreshold = spec.getDownThreshold();
  }

  @Override
  public void init(PercentageChangeRuleAnomalyFilterSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    List<MetricSlice> slices = new ArrayList<>();
    MetricSlice currentSlice =
        MetricSlice.from(-1L,
            anomaly.getStartTime(),
            anomaly.getEndTime(),
            ArrayListMultimap.create());
    // customize baseline offset
    if (baseline != null) {
      slices.addAll(this.baseline.scatter(currentSlice));
    }

    Map<MetricSlice, DataFrame> aggregates =
        this.dataFetcher.fetchData(new InputDataSpec().withAggregateSlices(slices)).getAggregates();

    double currentValue = anomaly.getAvgCurrentVal();
    double baselineValue;
    if (baseline == null) {
      baselineValue = anomaly.getAvgBaselineVal();
    } else {
      try {
        baselineValue = this.baseline.gather(currentSlice, aggregates)
            .getDouble(DataFrame.COL_VALUE, 0);
      } catch (Exception e) {
        baselineValue = anomaly.getAvgBaselineVal();
        LOG.warn(
            "Unable to fetch baseline for anomaly {}. start = {} end = {} filters = {}. Using anomaly"
                + " baseline ",
            anomaly.getId(),
            anomaly.getStartTime(),
            anomaly.getEndTime(),
            ArrayListMultimap.create(),
            e);
      }
    }

    // if inconsistent with up/down, filter the anomaly
    if (!pattern.equals(Pattern.UP_OR_DOWN) && (currentValue < baselineValue && pattern
        .equals(Pattern.UP)) || (
        currentValue > baselineValue && pattern.equals(Pattern.DOWN))) {
      return false;
    }

    double percentageChange = Math.abs(currentValue / baselineValue - 1);
    if (currentValue < baselineValue) {
      double downThreshold = Double.isNaN(this.downThreshold) ? this.threshold : this.downThreshold;
      return Double.compare(downThreshold, percentageChange) <= 0;
    } else {
      double upThreshold = Double.isNaN(this.upThreshold) ? this.threshold : this.upThreshold;
      return Double.compare(upThreshold, percentageChange) <= 0;
    }
  }
}
