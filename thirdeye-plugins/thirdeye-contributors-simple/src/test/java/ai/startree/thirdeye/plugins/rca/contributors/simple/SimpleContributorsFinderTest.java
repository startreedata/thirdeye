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
package ai.startree.thirdeye.plugins.rca.contributors.simple;

import static ai.startree.thirdeye.plugins.rca.contributors.simple.SimpleContributorsFinder.COL_CONTRIBUTION_CHANGE_PERCENTAGE;
import static ai.startree.thirdeye.plugins.rca.contributors.simple.SimpleContributorsFinder.COL_VALUE_CHANGE_PERCENTAGE;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NOT_ENOUGH_DATA_FOR_RCA;
import static ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi.ALL;
import static ai.startree.thirdeye.spi.datasource.loader.AggregationLoader.COL_DIMENSION_NAME;
import static ai.startree.thirdeye.spi.datasource.loader.AggregationLoader.COL_DIMENSION_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.api.cube.SummaryResponseRow;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import ai.startree.thirdeye.spi.rca.ContributorsSearchConfiguration;
import java.util.List;
import java.util.function.Supplier;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.mockito.ArgumentMatchers;
import org.testng.annotations.Test;

public class SimpleContributorsFinderTest {

  public static final DataFrame EMPTY_DATA_FRAME = DataFrame.builder(COL_TIME + ":LONG").build();
  private static final DateTime BASELINE_START = new DateTime(2022,
      1,
      1,
      0,
      0,
      0,
      DateTimeZone.UTC);
  private static final DateTime BASELINE_END = new DateTime(2022, 2, 1, 0, 0, 0, DateTimeZone.UTC);
  private static final Interval BASELINE_INTERVAL = new Interval(BASELINE_START, BASELINE_END);

  private static final DateTime CURRENT_START = new DateTime(2022, 1, 8, 0, 0, 0, DateTimeZone.UTC);
  private static final DateTime CURRENT_END = new DateTime(2022, 2, 8, 0, 0, 0, DateTimeZone.UTC);
  private static final Interval CURRENT_INTERVAL = new Interval(CURRENT_START, CURRENT_END);

  private static final MetricConfigDTO METRIC_CONFIG_DTO = new MetricConfigDTO();
  private static final DatasetConfigDTO DATASET_CONFIG_DTO = new DatasetConfigDTO();
  private static final int SUMMARY_SIZE = 2;
  private static final int DEPTH = 2; // not used by the algorithm
  private static final boolean DO_ONE_SIDE_ERROR = true;
  private static final List<Predicate> FILTERS = List.of();
  private static final List<List<String>> HIERARCHIES = null;

  private static final MetricSlice BASELINE_SLICE = MetricSlice.from(METRIC_CONFIG_DTO,
      BASELINE_INTERVAL,
      FILTERS,
      DATASET_CONFIG_DTO);
  private static final MetricSlice CURRENT_SLICE = MetricSlice.from(METRIC_CONFIG_DTO,
      CURRENT_INTERVAL,
      FILTERS,
      DATASET_CONFIG_DTO);

  // os in android, osx. browser in chrome, safari
  private static final Supplier<DataFrame> BASELINE_DATAFRAME = () -> new DataFrame().addSeries(COL_DIMENSION_NAME,
          StringSeries.buildFrom("os", "os", "browser", "browser"))
      .addSeries(COL_DIMENSION_VALUE, StringSeries.buildFrom("android", "osx", "chrome", "safari"))
      .addSeries(COL_VALUE, DoubleSeries.buildFrom(40, 60, 80, 20))
      .addSeries(COL_TIME, LongSeries.buildFrom(1L, 1L, 1L, 1L));

  private static final Supplier<DataFrame> CURRENT_DATAFRAME = () -> new DataFrame().addSeries(COL_DIMENSION_NAME,
          StringSeries.buildFrom("os", "os", "browser", "browser"))
      .addSeries(COL_DIMENSION_VALUE, StringSeries.buildFrom("android", "osx", "chrome", "safari"))
      .addSeries(COL_VALUE, DoubleSeries.buildFrom(50, 70, 50, 70))
      .addSeries(COL_TIME, LongSeries.buildFrom(2L, 2L, 2L, 2L));

  @Test
  public void testSearchNominalCase() throws Exception {
    final AggregationLoader aggregationLoader = mock(AggregationLoader.class);

    when(aggregationLoader.loadBreakdown(ArgumentMatchers.eq(BASELINE_SLICE), anyInt())).thenReturn(
        BASELINE_DATAFRAME.get());
    when(aggregationLoader.loadBreakdown(ArgumentMatchers.eq(CURRENT_SLICE), anyInt())).thenReturn(
        CURRENT_DATAFRAME.get());

    final SimpleContributorsFinder contributorsFinder = new SimpleContributorsFinder(
        aggregationLoader,
        new SimpleConfiguration());

    final ContributorsFinderResult output = contributorsFinder.search(new ContributorsSearchConfiguration(
        METRIC_CONFIG_DTO,
        DATASET_CONFIG_DTO,
        CURRENT_INTERVAL,
        BASELINE_INTERVAL,
        SUMMARY_SIZE,
        DEPTH,
        DO_ONE_SIDE_ERROR,
        FILTERS,
        HIERARCHIES));

    final DimensionAnalysisResultApi dimensionAnalysisResultOutput = output.getDimensionAnalysisResult();
    final List<SummaryResponseRow> rowsOutput = dimensionAnalysisResultOutput.getResponseRows();

    assertThat(rowsOutput).hasSize(SUMMARY_SIZE);

    assertThat(rowsOutput.get(0).getNames()).hasSameElementsAs(List.of("osx", ALL));
    assertThat(rowsOutput.get(0).getBaselineValue()).isEqualTo(60);
    assertThat(rowsOutput.get(0).getCurrentValue()).isEqualTo(70);
    assertThat(rowsOutput.get(1).getNames()).hasSameElementsAs(List.of(ALL, "safari"));
    assertThat(rowsOutput.get(1).getBaselineValue()).isEqualTo(20);
    assertThat(rowsOutput.get(1).getCurrentValue()).isEqualTo(70);
  }

  @Test
  public void testSearchThrowsErrorIfEmptyDataForBaselineTimeframe() throws Exception {
    final AggregationLoader aggregationLoader = mock(AggregationLoader.class);

    when(aggregationLoader.loadBreakdown(ArgumentMatchers.eq(BASELINE_SLICE), anyInt())).thenReturn(
        EMPTY_DATA_FRAME);
    final SimpleContributorsFinder contributorsFinder = new SimpleContributorsFinder(
        aggregationLoader,
        new SimpleConfiguration());

    assertThatThrownBy(() -> contributorsFinder.search(new ContributorsSearchConfiguration(
        METRIC_CONFIG_DTO,
        DATASET_CONFIG_DTO,
        CURRENT_INTERVAL,
        BASELINE_INTERVAL,
        SUMMARY_SIZE,
        DEPTH,
        DO_ONE_SIDE_ERROR,
        FILTERS,
        HIERARCHIES))).isInstanceOf(ThirdEyeException.class)
        .hasFieldOrPropertyWithValue("status", ERR_NOT_ENOUGH_DATA_FOR_RCA);
  }

  @Test
  public void testSearchThrowsErrorIfEmptyDataForCurrentTimeFrame() throws Exception {
    final AggregationLoader aggregationLoader = mock(AggregationLoader.class);
    when(aggregationLoader.loadBreakdown(ArgumentMatchers.eq(BASELINE_SLICE), anyInt())).thenReturn(
        BASELINE_DATAFRAME.get());
    when(aggregationLoader.loadBreakdown(ArgumentMatchers.eq(CURRENT_SLICE), anyInt())).thenReturn(
        EMPTY_DATA_FRAME);
    final SimpleContributorsFinder contributorsFinder = new SimpleContributorsFinder(
        aggregationLoader,
        new SimpleConfiguration());

    assertThatThrownBy(() -> contributorsFinder.search(new ContributorsSearchConfiguration(
        METRIC_CONFIG_DTO,
        DATASET_CONFIG_DTO,
        CURRENT_INTERVAL,
        BASELINE_INTERVAL,
        SUMMARY_SIZE,
        DEPTH,
        DO_ONE_SIDE_ERROR,
        FILTERS,
        HIERARCHIES))).isInstanceOf(ThirdEyeException.class)
        .hasFieldOrPropertyWithValue("status", ERR_NOT_ENOUGH_DATA_FOR_RCA);
  }

  @Test
  public void testMergeAndComputeStatsWithDifferentValuesInBreakdown() {
    final DataFrame baseline = new DataFrame()
        .addSeries(COL_DIMENSION_NAME, "dim1", "dim1")
        .addSeries(COL_DIMENSION_VALUE, "val1", "val2")
        .addSeries(COL_VALUE, 1D, 1D)
        ;
    final double baselineTotal = SimpleContributorsFinder.getTotalFromBreakdown(baseline);

    final DataFrame current = new DataFrame()
        .addSeries(COL_DIMENSION_NAME, "dim1", "dim1")
        .addSeries(COL_DIMENSION_VALUE, "val1", "val3") // value is different here
        .addSeries(COL_VALUE, 1D, 1D)
        ;
    final double currentTotal = SimpleContributorsFinder.getTotalFromBreakdown(current);

    final DataFrame stats = SimpleContributorsFinder.computeStats(baseline, baselineTotal, current, currentTotal);
    assertThat(stats.get(COL_VALUE_CHANGE_PERCENTAGE).getDoubles().values()).isEqualTo(new double[]{0D, -100D, 0d/0d});
    assertThat(stats.get(COL_CONTRIBUTION_CHANGE_PERCENTAGE).getDoubles().values()).isEqualTo(new double[]{0D, -50, 50});
  }

}
