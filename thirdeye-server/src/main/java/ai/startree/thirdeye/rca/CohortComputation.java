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

package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.rca.HeatmapCalculator.getSimpleRange;
import static ai.startree.thirdeye.spi.datalayer.Predicate.parseAndCombinePredicates;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.BreakdownApi;
import ai.startree.thirdeye.spi.api.DimensionFilterContributionApi;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

@Singleton
public class CohortComputation {

  private final HeatmapCalculator heatmapCalculator;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public CohortComputation(final HeatmapCalculator heatmapCalculator,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    this.heatmapCalculator = heatmapCalculator;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  public static DateTimeZone getDateTimeZone(String timezone) {
    return optional(timezone)
        .filter(StringUtils::isNotEmpty)
        .map(DateTimeZone::forID)
        .orElse(Constants.DEFAULT_TIMEZONE);
  }

  public BreakdownApi computeBreakdown(final BreakdownApi request,
      final List<String> filters,
      final Integer limit) throws Exception {
    final Interval currentInterval = new Interval(
        request.getStart(),
        request.getEnd(),
        getDateTimeZone(request.getTimezone()));

    final DatasetConfigDTO dataset = datasetConfigManager.findById(request.getDataset().getId());
    final MetricConfigDTO metric = metricConfigManager.findById(request.getMetric().getId());

    final Set<Map<String, String>> visited = new HashSet<>();
    final Map<String, String> dimensionFilters = ImmutableMap.of();
    final List<DimensionFilterContributionApi> results = computeBreakdown0(
        request.getThreshold(),
        filters,
        limit,
        currentInterval,
        dataset,
        metric,
        visited,
        dimensionFilters);
    return new BreakdownApi().setResults(results);
  }

  private List<DimensionFilterContributionApi> computeBreakdown0(
      final Double threshold,
      final List<String> filters,
      final Integer limit,
      final Interval currentInterval,
      final DatasetConfigDTO dataset,
      final MetricConfigDTO metric,
      final Set<Map<String, String>> visited,
      final Map<String, String> dimensionFilters)
      throws Exception {
    if (visited.contains(dimensionFilters)) {
      return Collections.emptyList();
    }
    visited.add(dimensionFilters);
    final var breakdown = heatmapCalculator.computeBreakdown(
        metric,
        parseAndCombinePredicates(filters),
        currentInterval,
        getSimpleRange(),
        limit,
        dataset);

    final List<DimensionFilterContributionApi> results = new ArrayList<>();
    for (var e : breakdown.entrySet()) {
      final String dimension = e.getKey();
      for (var entry : e.getValue().entrySet()) {
        final Double dimensionContribution = entry.getValue();
        if (dimensionContribution > threshold) {
          final String dimensionValue = entry.getKey();
          final Map<String, String> subDimensionFilters = ImmutableMap.<String, String>builder()
              .putAll(dimensionFilters)
              .put(dimension, dimensionValue)
              .build();

          if (!visited.contains(subDimensionFilters)) {

            results.add(new DimensionFilterContributionApi()
                .setValue(dimensionContribution)
                .setDimensionFilters(subDimensionFilters)
            );

            final List<String> subFilters = new ArrayList<>(filters);
            subFilters.add(dimension + "=" + dimensionValue);

            final List<DimensionFilterContributionApi> list = computeBreakdown0(
                threshold,
                subFilters,
                limit,
                currentInterval,
                dataset,
                metric,
                visited,
                subDimensionFilters);

            results.addAll(list);
          }
        }
      }
    }
    return results;
  }
}
