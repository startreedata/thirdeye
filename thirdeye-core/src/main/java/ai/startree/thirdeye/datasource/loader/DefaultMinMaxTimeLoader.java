/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.datasource.loader;

import static ai.startree.thirdeye.util.CalciteUtils.addAlias;
import static ai.startree.thirdeye.util.CalciteUtils.identifierDescOf;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.query.SelectQuery;
import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.util.CalciteUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultMinMaxTimeLoader implements MinMaxTimeLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultMinMaxTimeLoader.class);

  private static final String TIME_ALIAS = "timeMillis";

  private final DataSourceCache dataSourceCache;
  private final ExecutorService executorService;

  @Inject
  public DefaultMinMaxTimeLoader(final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
    executorService = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder().setNameFormat("minmax-loader-%d").build());
  }

  @Override
  public Future<@Nullable Long> fetchMinTimeAsync(final DatasetConfigDTO datasetConfigDTO,
      final @Nullable Interval timeFilterInterval) throws Exception {
    return executorService.submit(() -> fetchExtremumTime(Extremum.MIN, datasetConfigDTO,
        timeFilterInterval));
  }

  @Override
  public Future<@Nullable Long> fetchMaxTimeAsync(final DatasetConfigDTO datasetConfigDTO,
      final @Nullable Interval timeFilterInterval) throws Exception {
    return executorService.submit(
        () -> fetchExtremumTime(Extremum.MAX, datasetConfigDTO, timeFilterInterval));
  }

  private @Nullable Long fetchExtremumTime(final Extremum extremum,
      final DatasetConfigDTO datasetConfigDTO, final @Nullable Interval timeFilterInterval)
      throws Exception {
    final String dataSourceName = Objects.requireNonNull(datasetConfigDTO.getDataSource());
    final @NonNull ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceName);
    final String sqlQuery = extremumTimeSqlQuery(datasetConfigDTO, dataSource, extremum,
        timeFilterInterval);
    final Map<String, String> customOptions = Map.of(); // custom query options not implemented in MinMaxTimeLoader
    final DataSourceRequest request = new DataSourceRequest(null, sqlQuery, customOptions,
        Map.of());
    final DataFrame df = dataSource.fetchDataTable(request).getDataFrame();
    if (df == null || df.size() == 0) {
      LOG.warn(
          "Empty dataframe for {} time query on dataset {} on interval {}. Dataset is empty or unknown SQL error. Could not fetch start time.",
          extremum, datasetConfigDTO.getDataset(),
          timeFilterInterval == null ? "full dataset" : timeFilterInterval.toString());
      return null;
    }

    return df.getLong(TIME_ALIAS, 0);
  }

  /**
   * The resulting query looks like this:
   *
   * SELECT
   * DATETIMECONVERT(dateTimeCol, '1:DAYS:SIMPLE_DATE_FORMAT:yyyy-MM-dd''T''HH:mm:ss.SSS''Z''',
   * '1:MILLISECONDS:EPOCH', '1:MILLISECONDS')) AS minTime
   * FROM callanalyzer_result_metrics
   * order by dateTimeCol
   * LIMIT 1
   *
   * To get the max, the order clause is: order by dateTimeCol desc
   *
   * This query ensures the segment time column metadata is hit.
   * Pinot process segments in parallel. Given that dateTimeCol is sorted, if the server has 4
   * threads per query,it will start processing 4 segments, pick the first row for each one (because
   * it is sorted by the column) then it will update some processing info saying skip all segments
   * whose min value is greater than the one I found. Data is distributed in segment
   * ordered by dateTimeCol, so Pinot will prune these segments. Given that this is done
   * concurrently, it is expected that a small number of segments are scanned, because there is a
   * race condition between the different server threads, but this is fine.
   */
  private String extremumTimeSqlQuery(final DatasetConfigDTO datasetConfigDTO,
      final ThirdEyeDataSource thirdEyeDataSource, final Extremum extremum,
      final Interval timeFilterInterval) {
    final SqlExpressionBuilder sqlExpressionBuilder = thirdEyeDataSource.getSqlExpressionBuilder();
    final SqlLanguage sqlLanguage = thirdEyeDataSource.getSqlLanguage();
    final SqlDialect dialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());
    final Config sqlParserConfig = SqlLanguageTranslator.translate(
        sqlLanguage.getSqlParserConfig());

    final SqlNode projection = getTimeColumnToMillisProjection(datasetConfigDTO,
        sqlExpressionBuilder, dialect, sqlParserConfig);

    final SqlNode orderByNode = extremum.orderByNode(datasetConfigDTO.getTimeColumn());

    final SelectQuery calciteRequestBuilder = new SelectQuery(datasetConfigDTO.getDataset()).select(
        projection).orderBy(orderByNode).limit(1);

    if (timeFilterInterval != null) {
      calciteRequestBuilder.whereTimeFilter(timeFilterInterval, datasetConfigDTO.getTimeColumn(),
          datasetConfigDTO.getTimeFormat());
    }

    return calciteRequestBuilder.build().getSql(sqlLanguage, sqlExpressionBuilder);
  }

  // fixme cyril code is duplicated with timeAggregation in calciteRequest - remove this see CalciteRequest L212
  private SqlNode getTimeColumnToMillisProjection(final DatasetConfigDTO datasetConfigDTO,
      final SqlExpressionBuilder sqlExpressionBuilder, final SqlDialect dialect,
      final SqlParser.Config sqlParserConfig) {
    final String quoteSafeTimeColumn = dialect.quoteIdentifier(datasetConfigDTO.getTimeColumn());
    final String timeGroupExpression = sqlExpressionBuilder.getTimeGroupExpression(
        quoteSafeTimeColumn, datasetConfigDTO.getTimeFormat(), Period.millis(1), DateTimeZone.UTC.toString());

    final SqlNode timeGroupNode = CalciteUtils.expressionToNode(timeGroupExpression,
        sqlParserConfig);
    return addAlias(timeGroupNode, TIME_ALIAS);
  }

  private enum Extremum {
    MIN {
      @Override
      @NonNull SqlNode orderByNode(final String timeColumn) {
        return identifierOf(timeColumn);
      }
    }, MAX {
      @Override
      @NonNull SqlNode orderByNode(final String timeColumn) {
        return identifierDescOf(timeColumn);
      }
    };

    abstract @NonNull SqlNode orderByNode(final String timeColumn);
  }
}
