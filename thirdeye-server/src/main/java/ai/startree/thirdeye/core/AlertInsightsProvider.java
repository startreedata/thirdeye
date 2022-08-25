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
package ai.startree.thirdeye.core;

import static ai.startree.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
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
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.checkerframework.checker.nullness.qual.NonNull;
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

  public AlertInsightsApi getInsights(final AlertDTO alertDTO)
      throws Exception {
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO,
          NOT_USED_INTERVAL);
      final AlertMetadataDTO metadata = templateWithProperties.getMetadata();

      final AlertInsightsApi insights = new AlertInsightsApi().setTemplateWithProperties(
          toAlertTemplateApi(templateWithProperties));
      addDatasetTimes(insights, metadata);

      return insights;
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
      LOG.warn("Dataset not found: {}. Cannot fetch start and end time.", datasetName);
      return;
    }
    final String dataSource = datasetConfigDTO.getDataSource();
    if (dataSource == null) {
      LOG.warn("Datasource is null in dataset configuration: {}. Cannot fetch start and end time.",
          datasetName);
      return;
    }

    final @NonNull ThirdEyeDataSource thirdEyeDataSource = dataSourceCache.getDataSource(dataSource);
    final SqlExpressionBuilder sqlExpressionBuilder = thirdEyeDataSource.getSqlExpressionBuilder();
    final SqlLanguage sqlLanguage = thirdEyeDataSource.getSqlLanguage();

    final String sqlQuery = minMaxTimesSqlQuery(datasetConfigDTO,
        sqlExpressionBuilder,
        sqlLanguage);
    final DataSourceRequest request = new DataSourceRequest(null, sqlQuery, Map.of());
    final DataFrame df = thirdEyeDataSource.fetchDataTable(request).getDataFrame();
    if (df.size() == 0) {
      LOG.warn(
          "Empty dataframe for max/min time query on dataset {}. Dataset is empty or unknown SQL error. Could not fetch start and end time.",
          datasetName);
      return;
    }
    final long datasetStartTime = df.getLong(MIN_TIME_ALIAS, 0);
    final long datasetEndTime = df.getLong(MAX_TIME_ALIAS, 0);
    final Interval defaultInterval = getDefaultInterval(datasetStartTime, datasetEndTime, metadata);

    insights.setDatasetEndTime(datasetEndTime);
    insights.setDatasetStartTime(datasetStartTime);
    insights.setDefaultStartTime(defaultInterval.getStartMillis());
    insights.setDefaultEndTime(defaultInterval.getEndMillis());
  }

  @VisibleForTesting
  protected static Interval getDefaultInterval(final long datasetStartTime,
      final long datasetEndTime, @NonNull final AlertMetadataDTO metadata) {
    final Period granularity = isoPeriod(metadata.getGranularity());
    final DateTimeZone timeZone = optional(metadata.getTimezone()).map(DateTimeZone::forID)
        .orElse(Constants.DEFAULT_TIMEZONE);
    DateTime defaultEndDateTime = new DateTime(datasetEndTime, timeZone);
    defaultEndDateTime = TimeUtils.floorByPeriod(defaultEndDateTime, granularity);

    DateTime defaultStartTime = defaultEndDateTime.minus(defaultChartTimeframe(granularity));
    if (defaultStartTime.getMillis() < datasetStartTime) {
      defaultStartTime = new DateTime(datasetStartTime, timeZone);
      defaultStartTime = TimeUtils.floorByPeriod(defaultStartTime, granularity);
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
      final SqlExpressionBuilder sqlExpressionBuilder, final SqlLanguage sqlLanguage)
      throws SqlParseException {
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
    final CalciteRequest calciteRequest = CalciteRequest.newBuilder(datasetConfigDTO.getDataset())
        .addSelectProjection(maxTimeProjection)
        .addSelectProjection(minTimeProjection)
        .build();

    return calciteRequest.getSql(sqlLanguage, sqlExpressionBuilder);
  }
}
