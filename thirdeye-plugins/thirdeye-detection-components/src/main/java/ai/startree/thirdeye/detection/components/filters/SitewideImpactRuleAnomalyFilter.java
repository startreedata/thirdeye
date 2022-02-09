/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import ai.startree.thirdeye.spi.detection.BaselineParsingUtils;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.model.InputData;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import ai.startree.thirdeye.spi.rootcause.timeseries.Baseline;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Site-wide impact anomaly filter
 */
public class SitewideImpactRuleAnomalyFilter implements
    AnomalyFilter<SitewideImpactRuleAnomalyFilterSpec> {

  private double threshold;
  private InputDataFetcher dataFetcher;
  private Baseline baseline;
  private String siteWideMetricUrn;
  private Pattern pattern;

  @Override
  public void init(SitewideImpactRuleAnomalyFilterSpec spec) {
    this.threshold = spec.getThreshold();
    Preconditions.checkArgument(Math.abs(this.threshold) <= 1,
        "Site wide impact threshold should be less or equal than 1");

    this.pattern = Pattern.valueOf(spec.getPattern().toUpperCase());

    // customize baseline offset
    if (StringUtils.isNotBlank(spec.getOffset())) {
      this.baseline = BaselineParsingUtils.parseOffset(spec.getOffset(), spec.getTimezone());
    }

    if (!Strings.isNullOrEmpty(spec.getSitewideCollection()) && !Strings
        .isNullOrEmpty(spec.getSitewideMetricName())) {
      // build filters
      Map<String, Collection<String>> filterMaps = spec.getFilters();
      Multimap<String, String> filters = ArrayListMultimap.create();
      if (filterMaps != null) {
        for (Map.Entry<String, Collection<String>> entry : filterMaps.entrySet()) {
          filters.putAll(entry.getKey(), entry.getValue());
        }
      }

      // build site wide metric Urn
      InputDataSpec.MetricAndDatasetName metricAndDatasetName =
          new InputDataSpec.MetricAndDatasetName(spec.getSitewideMetricName(),
              spec.getSitewideCollection());
      InputData data = this.dataFetcher.fetchData(
          new InputDataSpec()
              .withMetricNamesAndDatasetNames(Collections.singletonList(metricAndDatasetName)));
      MetricConfigDTO metricConfigDTO = data.getMetricForMetricAndDatasetNames()
          .get(metricAndDatasetName);
      MetricEntity me = MetricEntity.fromMetric(1.0, metricConfigDTO.getId(), filters);
      this.siteWideMetricUrn = me.getUrn();
    }
  }

  @Override
  public void init(SitewideImpactRuleAnomalyFilterSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    MetricEntity me = MetricEntity.fromURN(anomaly.getMetricUrn());
    List<MetricSlice> slices = new ArrayList<>();
    MetricSlice currentSlice = MetricSlice
        .from(me.getId(), anomaly.getStartTime(), anomaly.getEndTime(), me.getFilters());

    // customize baseline offset
    MetricSlice baselineSlice = null;
    if (baseline != null) {
      baselineSlice = this.baseline.scatter(currentSlice).get(0);
      slices.add(baselineSlice);
    }

    MetricSlice siteWideSlice;
    if (Strings.isNullOrEmpty(this.siteWideMetricUrn)) {
      // if global metric is not set
      MetricEntity siteWideEntity = MetricEntity.fromURN(anomaly.getMetricUrn());
      siteWideSlice = MetricSlice
          .from(siteWideEntity.getId(), anomaly.getStartTime(), anomaly.getEndTime());
    } else {
      MetricEntity siteWideEntity = MetricEntity.fromURN(this.siteWideMetricUrn);
      siteWideSlice = MetricSlice
          .from(siteWideEntity.getId(), anomaly.getStartTime(), anomaly.getEndTime(),
              siteWideEntity.getFilters());
    }
    slices.add(siteWideSlice);

    Map<MetricSlice, DataFrame> aggregates = this.dataFetcher.fetchData(
        new InputDataSpec().withAggregateSlices(slices))
        .getAggregates();

    double currentValue = anomaly.getAvgCurrentVal();
    double baselineValue = baseline == null ? anomaly.getAvgBaselineVal()
        : this.baseline.gather(currentSlice, aggregates).getDouble(
            DataFrame.COL_VALUE, 0);
    double siteWideValue = getValueFromAggregates(siteWideSlice, aggregates);

    // if inconsistent with up/down, filter the anomaly
    if (!pattern.equals(Pattern.UP_OR_DOWN) && (currentValue < baselineValue && pattern
        .equals(Pattern.UP)) || (currentValue > baselineValue && pattern.equals(Pattern.DOWN))) {
      return false;
    }
    // if doesn't pass the threshold, filter the anomaly
    return siteWideValue == 0
        || !((Math.abs(currentValue - baselineValue) / siteWideValue) < this.threshold);
  }

  private double getValueFromAggregates(MetricSlice slice, Map<MetricSlice, DataFrame> aggregates) {
    return aggregates.get(slice).getDouble(DataFrame.COL_VALUE, 0);
  }
}
