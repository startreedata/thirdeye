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

package org.apache.pinot.thirdeye.datasource.pinotsql;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.DataSourceUtils;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import org.apache.pinot.thirdeye.spi.detection.TimeSpec;
import org.apache.pinot.thirdeye.spi.util.SpiUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains methods to return max date time for datasets in pinot
 */
public class PinotSqlDataSourceTimeQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(PinotSqlDataSourceTimeQuery.class);

  private final static String TIME_QUERY_TEMPLATE = "SELECT %s(%s) FROM %s WHERE %s";

  /**
   * Returns the max time in millis for dataset in pinot
   *
   * @return max date time in millis
   */
  public static long getMaxDateTime(final DatasetConfigDTO datasetConfig, PinotSqlThirdEyeDataSource dataSource) {
    long maxTime = queryTimeSpecFromPinot("max", datasetConfig, dataSource);
    if (maxTime <= 0) {
      maxTime = System.currentTimeMillis();
    }
    return maxTime;
  }

  /**
   * Returns the earliest time in millis for a dataset in pinot
   *
   * @return min (earliest) date time in millis. Returns 0 if dataset is not found
   */
  public static long getMinDateTime(final DatasetConfigDTO datasetConfig, PinotSqlThirdEyeDataSource dataSource) {
    return queryTimeSpecFromPinot("min", datasetConfig, dataSource);
  }

  private static long queryTimeSpecFromPinot(final String functionName,
      final DatasetConfigDTO datasetConfig, PinotSqlThirdEyeDataSource dataSource) {
    long maxTime = 0;
    String dataset = datasetConfig.getName();
    try {
      // By default, query only offline, unless dataset has been marked as realtime
      TimeSpec timeSpec = DataSourceUtils.getTimestampTimeSpecFromDatasetConfig(datasetConfig);

      long cutoffTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
      String timeClause = SqlUtils.getBetweenClause(new DateTime(0, DateTimeZone.UTC),
          new DateTime(cutoffTime, DateTimeZone.UTC),
          timeSpec,
          datasetConfig);

      String maxTimePql = String
          .format(TIME_QUERY_TEMPLATE, functionName, timeSpec.getColumnName(), dataset, timeClause);
      PinotSqlQuery maxTimePinotQuery = new PinotSqlQuery(maxTimePql, dataset);

      ThirdEyeResultSetGroup resultSetGroup;
      dataSource.refreshSQL(maxTimePinotQuery);
      resultSetGroup = dataSource.executeSQL(maxTimePinotQuery);

      if (resultSetGroup.size() == 0 || resultSetGroup.get(0).getRowCount() == 0) {
        LOGGER.error("Failed to get latest max time for dataset {} with SQL: {}", dataset,
            maxTimePinotQuery.getQuery());
      } else {
        DateTimeZone timeZone = SpiUtils.getDateTimeZone(datasetConfig);

        long endTime = new Double(resultSetGroup.get(0).getDouble(0)).longValue();
        // endTime + 1 to make sure we cover the time range of that time value.
        String timeFormat = timeSpec.getFormat();
        if (StringUtils.isBlank(timeFormat) || TimeSpec.SINCE_EPOCH_FORMAT.equals(timeFormat)) {
          maxTime = timeSpec.getDataGranularity().toMillis(endTime + 1, timeZone) - 1;
        } else {
          DateTimeFormatter inputDataDateTimeFormatter =
              DateTimeFormat.forPattern(timeFormat).withZone(timeZone);
          DateTime endDateTime = DateTime
              .parse(String.valueOf(endTime), inputDataDateTimeFormatter);
          Period oneBucket = datasetConfig.bucketTimeGranularity().toPeriod();
          maxTime = endDateTime.plus(oneBucket).getMillis() - 1;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Exception getting maxTime from collection: {}", dataset, e);
    }
    return maxTime;
  }
}
