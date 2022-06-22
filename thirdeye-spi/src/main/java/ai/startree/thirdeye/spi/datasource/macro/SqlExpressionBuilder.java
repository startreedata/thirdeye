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
package ai.startree.thirdeye.spi.datasource.macro;

import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * A provider of SQL expression generation methods.
 * Used to expand macro functions into valid SQL.
 */
public interface SqlExpressionBuilder {

  /**
   * Generates a SQL expression that test if a time column is between bounds.
   * Implementation should be careful with identifier quoting.
   * Identifiers may be passed quoted or unquoted.
   *
   * Implementation should respect the filterInterval timezone.
   *
   * For example: {@code getTimeFilterExpression(myTimeEpoch, Interval(42,84), "EPOCH_MILLIS")} returns
   * "myTimeEpoch >= 42 AND myTimeEpoch < 84"
   *
   * Used by the macro __timeFilter().
   *
   * @param timeColumn time column name
   * @param filterInterval interval to filter on.
   * @param timeColumnFormat time column time format. Managed formats depend on the datasource
   */
  default String getTimeFilterExpression(final String timeColumn, final Interval filterInterval,
      final String timeColumnFormat) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generates a SQL expression that test if a time column is between bounds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   *
   * Used internally to generate queries. For RCA for instance.
   * Implementation should respect the filterInterval timezone.
   *
   * timeFormat comes from DatasetConfigDTO$format, so it can be anything. It is specific to the
   * datasource.
   * The function should be able to parse the timeFormat used by the datasource.
   * If it's null, the function should assume the format is epoch milliseconds.
   *
   * This method can either generate the datetime in the given format directly,
   * or generate a SQL that transforms the colum or the milliseconds in the correct format at
   * runtime.
   *
   * @param timeColumn time column name
   * @param filterInterval interval to filter on.
   * @param timeFormat any string, coming from DatasetConfigDTO$format - the datasource is free
   *     to put any format in DatasetConfigDTO$format.
   * @param timeUnit the String of a TimeUnit, coming from DatasetConfigDTO$timeUnit.
   */
  default String getTimeFilterExpression(final String timeColumn, final Interval filterInterval,
      final @Nullable String timeFormat,
      @Nullable final String timeUnit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   * Round with the given timezone. If timezone is null, keep the default timezone
   * behavior of the datasource.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * For example: for a MySQL implementation, {@code getTimeGroupExpression(dateTimeColumn,
   * DATETIME, P1D)} returns "UNIX_TIMESTAMP(DATE(dateTimeColumn))*1000"
   *
   * Used by macro __timeGroup()
   *
   * @param timeColumn time column name
   * @param timeColumnFormat time column time format. Managed formats depend on the datasource
   * @param granularity granularity of the output epoch milliseconds (minutes, days, etc...)
   * @param timezone timezone string in tz database format
   */
  default String getTimeGroupExpression(final String timeColumn, final String timeColumnFormat,
      final Period granularity, @Nullable final String timezone) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * Round with the given timezone. If timezone is null, keep the default timezone
   * behavior of the datasource.
   *
   * timeFormat comes from DatasetConfigDTO$format, so it can be anything. It is specific to the
   * datasource.
   * The function should be able to parse the timeFormat used by the datasource.
   * If it's null, the function should assume the input format is epoch milliseconds.
   *
   * Used internally to generate queries. For RCA for instance.
   *
   * @param timeFormat any string, coming from DatasetConfigDTO$format - the datasource is free
   *     to put any format in DatasetConfigDTO$format.
   * @param timeUnit the String of a TimeUnit, coming from DatasetConfigDTO$timeUnit.
   * @param timezone timezone string in tz database format
   */
  default String getTimeGroupExpression(final String timeColumn, @Nullable final String timeFormat,
      final Period granularity, @Nullable final String timeUnit, @Nullable final String timezone) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns whether a MetricAggFunction requires a custom SQL statement or
   * can be safely generated with ANSI SQL.
   */
  default boolean needsCustomDialect(final MetricAggFunction metricAggFunction) {
    switch (metricAggFunction) {
      case PCT50:
      case PCT90:
      case PCT95:
      case PCT99:
        return true;
      default:
        return false;
    }
  }

  /**
   * Generate a SQL expression for the given metric aggregation function, with the given operands.
   */
  default String getCustomDialectSql(final MetricAggFunction metricAggFunction,
      final List<String> operands,
      String quantifier) {
    throw new UnsupportedOperationException();
  }
}
