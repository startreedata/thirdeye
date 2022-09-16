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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;
import static ai.startree.thirdeye.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest;
import ai.startree.thirdeye.datasource.calcite.QueryProjection;
import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertInsightsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(AlertInsightsProvider.class);

  private static final Interval NOT_USED_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);
  private static final String MAX_TIME_ALIAS = "maxTime";
  private static final String MIN_TIME_ALIAS = "minTime";
  // computer clock difference is usually order of seconds - but here taking 1 day is safe and does not impact the logic
  private static final long COMPUTER_CLOCK_MARGIN_MILLIS = 86_400_000;

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourceCache dataSourceCache;

  @Inject
  public AlertInsightsProvider(final AlertTemplateRenderer alertTemplateRenderer,
      final DatasetConfigManager datasetConfigManager, final DataSourceCache dataSourceCache) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourceCache = dataSourceCache;
  }

  public AlertInsightsApi getInsights(final AlertDTO alertDTO) {
    try {
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO,
          NOT_USED_INTERVAL);
      final AlertMetadataDTO metadata = templateWithProperties.getMetadata();

      final AlertInsightsApi insights = new AlertInsightsApi().setTemplateWithProperties(
          toAlertTemplateApi(templateWithProperties));
      addDatasetTimes(insights, metadata);

      return insights;
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      // can do better exception handling if necessary - see handleAlertEvaluationException
      throw serverError(ERR_UNKNOWN, e);
    }
  }

  private void addDatasetTimes(@NonNull final AlertInsightsApi insights,
      @NonNull final AlertMetadataDTO metadata) throws Exception {
    final String datasetName = metadata.getDataset().getDataset();
    if (datasetName == null) {
      LOG.warn("Dataset name not found in alert metadata. Cannot fetch start and end time.");
      return;
    }
    final DatasetConfigDTO datasetConfigDTO = datasetConfigManager.findByDataset(datasetName);
    if (datasetConfigDTO == null) {
      LOG.warn(
          "Dataset configuration not found: {}. Dataset not onboarded? Cannot fetch start and end time.",
          datasetName);
      return;
    }

    // fetch dataset interval
    final DateTimeZone timeZone = optional(metadata.getTimezone()).map(DateTimeZone::forID)
        .orElse(Constants.DEFAULT_TIMEZONE);
    final Interval datasetInterval = getDatasetInterval(insights, datasetConfigDTO, timeZone);
    if (datasetInterval == null) {
      return;
    }

    // compute default chart interval
    final Period granularity = isoPeriod(metadata.getGranularity());
    final Interval defaultInterval = getDefaultChartInterval(datasetInterval, granularity);

    insights.setDatasetStartTime(datasetInterval.getStartMillis());
    insights.setDatasetEndTime(datasetInterval.getEndMillis());
    insights.setDefaultStartTime(defaultInterval.getStartMillis());
    insights.setDefaultEndTime(defaultInterval.getEndMillis());
  }

  private @Nullable Interval getDatasetInterval(final AlertInsightsApi insights,
      final DatasetConfigDTO datasetConfigDTO, final DateTimeZone timeZone) throws Exception {
    final DataFrame timesDf = fetchMinMaxTimes(datasetConfigDTO, null);
    if (timesDf == null || timesDf.size() == 0) {
      return null;
    }
    final long datasetStartTime = timesDf.getLong(MIN_TIME_ALIAS, 0);
    long datasetEndTime = timesDf.getLong(MAX_TIME_ALIAS, 0);

    // if there is bad data in the dataset, datasetEndTime can have an incorrect value, bigger than the current time - see TE-860
    // if it's the case - fetch a safe datasetEndTime value
    final long maximumPossibleEndTime = currentMaximumPossibleEndTime();
    final boolean endTimeIsInTheFuture = datasetEndTime > maximumPossibleEndTime;
    if (endTimeIsInTheFuture) {
      LOG.warn(
          "Dataset maxTime is too big: {}. Current system time: {}.Most likely a data issue in the dataset. Rerunning query with a filter < safeEndTime={} to get a safe maxTime.",
          datasetEndTime,
          System.currentTimeMillis(),
          maximumPossibleEndTime);
      insights.setSuspiciousDatasetEndTime(datasetEndTime);
      final Interval safeTimeInterval = new Interval(0L, maximumPossibleEndTime);
      final DataFrame safeTimesDf = fetchMinMaxTimes(datasetConfigDTO, safeTimeInterval);
      if (safeTimesDf == null || safeTimesDf.size() == 0) {
        LOG.warn("Could not fetch the maxTime on a safe time interval for dataset {}.",
            datasetConfigDTO.getDataset());
        return null;
      } else {
        datasetEndTime = safeTimesDf.getLong(MAX_TIME_ALIAS, 0);
      }
    }
    return new Interval(datasetStartTime, datasetEndTime, timeZone);
  }

  public static long currentMaximumPossibleEndTime() {
    return System.currentTimeMillis() + COMPUTER_CLOCK_MARGIN_MILLIS;
  }

  private @Nullable DataFrame fetchMinMaxTimes(final @NonNull DatasetConfigDTO datasetConfigDTO,
      final @Nullable Interval timeFilterInterval) throws Exception {
    final String dataSource = datasetConfigDTO.getDataSource();
    if (dataSource == null) {
      LOG.warn(
          "Datasource is null in dataset configuration: {}. Could not fetch start and end time.",
          datasetConfigDTO.getDataset());
      return null;
    }

    final @NonNull ThirdEyeDataSource thirdEyeDataSource = dataSourceCache.getDataSource(dataSource);

    final String sqlQuery = minMaxTimesSqlQuery(datasetConfigDTO,
        thirdEyeDataSource,
        timeFilterInterval);
    final DataSourceRequest request = new DataSourceRequest(null, sqlQuery, Map.of());

    final DataFrame df = thirdEyeDataSource.fetchDataTable(request).getDataFrame();

    if (df.size() == 0) {
      LOG.warn(
          "Empty dataframe for max/min time query on dataset {} on interval: {}. Dataset is empty or unknown SQL error. Could not fetch start and end time.",
          datasetConfigDTO.getDataset(),
          timeFilterInterval == null ? "full dataset" : timeFilterInterval.toString());
    }

    return df;
  }

  @VisibleForTesting
  protected static Interval getDefaultChartInterval(final @NonNull Interval datasetInterval,
      @NonNull final Period granularity) {
    final DateTime defaultEndDateTime = TimeUtils.floorByPeriod(datasetInterval.getEnd(),
        granularity);

    DateTime defaultStartTime = defaultEndDateTime.minus(defaultChartTimeframe(granularity));
    if (defaultStartTime.getMillis() < datasetInterval.getStartMillis()) {
      defaultStartTime = TimeUtils.floorByPeriod(datasetInterval.getStart(), granularity);
      // first bucket may be incomplete - start from second one
      defaultStartTime = defaultStartTime.plus(granularity);
    }

    return new Interval(defaultStartTime, defaultEndDateTime);
  }

  /**
   * Returns a default Period for the UI timeseries chart timeframe, based on the alert granularity.
   * Rule of thumb.
   */
  private static Period defaultChartTimeframe(final Period alertGranularity) {
    final long granularityMillis = alertGranularity.toStandardDuration().getMillis();
    if (granularityMillis < Period.hours(1).toStandardDuration().getMillis()) {
      return Period.months(1);
    } else if (granularityMillis < Period.days(1).toStandardDuration().getMillis()) {
      return Period.months(6);
    } else if (granularityMillis < Period.weeks(1).toStandardDuration().getMillis()) {
      return Period.years(1);
    } else if (granularityMillis < Period.months(1).toStandardDuration().getMillis()) {
      return Period.years(2);
    }
    return Period.years(4);
  }

  private String minMaxTimesSqlQuery(final DatasetConfigDTO datasetConfigDTO,
      final @NonNull ThirdEyeDataSource thirdEyeDataSource,
      final @Nullable Interval timeFilterInterval) throws SqlParseException {
    final SqlExpressionBuilder sqlExpressionBuilder = thirdEyeDataSource.getSqlExpressionBuilder();
    final SqlLanguage sqlLanguage = thirdEyeDataSource.getSqlLanguage();

    final SqlDialect dialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());
    // build sql string that transform time column in milliseconds
    // time conversion happens before the MAX operation - the datasource is responsible for optimizing this
    // Pinot does - order-preserving function optimization in max/min should be implemented by most DB systems
    final String timeColumnToMillisProjection = sqlExpressionBuilder.getTimeGroupExpression(dialect.quoteIdentifier(
            datasetConfigDTO.getTimeColumn()),
        datasetConfigDTO.getTimeFormat(),
        Period.millis(1),
        datasetConfigDTO.getTimeUnit().toString(),
        DateTimeZone.UTC.toString());

    final QueryProjection maxTimeProjection = QueryProjection.of(MetricAggFunction.MAX.toString(),
        List.of(timeColumnToMillisProjection)).withAlias(MAX_TIME_ALIAS);
    final QueryProjection minTimeProjection = QueryProjection.of(MetricAggFunction.MIN.toString(),
        List.of(timeColumnToMillisProjection)).withAlias(MIN_TIME_ALIAS);
    final CalciteRequest.Builder calciteRequestBuilder = CalciteRequest.newBuilder(datasetConfigDTO.getDataset())
        .addSelectProjection(maxTimeProjection)
        .addSelectProjection(minTimeProjection);

    if (timeFilterInterval != null) {
      calciteRequestBuilder.withTimeFilter(timeFilterInterval,
          datasetConfigDTO.getTimeColumn(),
          datasetConfigDTO.getTimeFormat(),
          datasetConfigDTO.getTimeUnit().name());
    }

    return calciteRequestBuilder.build().getSql(sqlLanguage, sqlExpressionBuilder);
  }
}
