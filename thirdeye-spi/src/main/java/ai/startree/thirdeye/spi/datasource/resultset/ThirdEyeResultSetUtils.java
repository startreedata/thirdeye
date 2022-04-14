/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.resultset;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceUtils;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeResultSetUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeResultSetUtils.class);

  public static List<String[]> parseResultSets(ThirdEyeRequest request,
      Map<MetricFunction, List<ThirdEyeResultSet>> metricFunctionToResultSetList,
      String sourceName) {

    int numGroupByKeys = 0;
    boolean hasGroupBy = false;
    if (request.getGroupByTimeGranularity() != null) {
      numGroupByKeys += 1;
    }
    if (request.getGroupBy() != null) {
      numGroupByKeys += request.getGroupBy().size();
    }
    if (numGroupByKeys > 0) {
      hasGroupBy = true;
    }
    int numMetrics = request.getMetricFunction().size();
    int numCols = numGroupByKeys + numMetrics;
    boolean hasGroupByTime = false;
    if (request.getGroupByTimeGranularity() != null) {
      hasGroupByTime = true;
    }

    int position = 0;
    Map<String, String[]> dataMap = new HashMap<>();
    Map<String, Integer> countMap = new HashMap<>();
    for (Map.Entry<MetricFunction, List<ThirdEyeResultSet>> entry : metricFunctionToResultSetList
        .entrySet()) {

      MetricFunction metricFunction = entry.getKey();

      DatasetConfigDTO datasetConfig = metricFunction.getDatasetConfig();
      TimeSpec dataTimeSpec = DataSourceUtils.getTimestampTimeSpecFromDatasetConfig(datasetConfig);

      long startTime = request.getStartTimeInclusive().getMillis();
      DateTimeZone dateTimeZone = SpiUtils.getDateTimeZone(datasetConfig);
      DateTime startDateTime = new DateTime(startTime, dateTimeZone);

      TimeGranularity dataGranularity = dataTimeSpec.getDataGranularity();
      boolean isISOFormat = false;
      DateTimeFormatter inputDataDateTimeFormatter = null;
      String timeFormat = dataTimeSpec.getFormat();
      if (timeFormat != null && !timeFormat.equals(TimeSpec.SINCE_EPOCH_FORMAT)) {
        isISOFormat = true;
        inputDataDateTimeFormatter = DateTimeFormat.forPattern(timeFormat).withZone(dateTimeZone);
      }

      List<ThirdEyeResultSet> resultSets = entry.getValue();
      for (int i = 0; i < resultSets.size(); i++) {
        ThirdEyeResultSet resultSet = resultSets.get(i);
        int numRows = resultSet.getRowCount();
        for (int r = 0; r < numRows; r++) {
          boolean skipRowDueToError = false;
          String[] groupKeys;
          String timestamp = null;
          if (hasGroupBy) {
            groupKeys = new String[resultSet.getGroupKeyLength()];
            for (int grpKeyIdx = 0; grpKeyIdx < resultSet.getGroupKeyLength(); grpKeyIdx++) {
              String groupKeyVal = "";
              try {
                groupKeyVal = resultSet.getGroupKeyColumnValue(r, grpKeyIdx);
              } catch (Exception e) {
                // IGNORE FOR NOW, workaround for Pinot Bug
              }
              if (hasGroupByTime && grpKeyIdx == 0) {
                int timeBucket;
                long millis;
                if (!isISOFormat) {
                  millis = dataGranularity.toMillis(Double.valueOf(groupKeyVal).longValue());
                } else {
                  millis = DateTime.parse(groupKeyVal, inputDataDateTimeFormatter).getMillis();
                }
                if (millis < startTime) {
                  LOG.error("Data point earlier than requested start time {}: {}",
                      new Date(startTime), new Date(millis));
                  skipRowDueToError = true;
                  break;
                }
                timeBucket = SpiUtils
                    .computeBucketIndex(request.getGroupByTimeGranularity(), startDateTime,
                        new DateTime(millis, dateTimeZone));
                groupKeyVal = String.valueOf(timeBucket);
                timestamp = String.valueOf(millis);
              }
              groupKeys[grpKeyIdx] = groupKeyVal;
            }
            if (skipRowDueToError) {
              continue;
            }
          } else {
            groupKeys = new String[]{};
          }
          String compositeGroupKey = StringUtils.join(groupKeys, "|");

          String[] rowValues = dataMap.get(compositeGroupKey);
          if (rowValues == null) {
            // add one to include the timestamp, if applicable
            if (timestamp != null && useCentralizedCache()) {
              rowValues = new String[numCols + 1];
            } else {
              rowValues = new String[numCols];
            }
            Arrays.fill(rowValues, "0");
            System.arraycopy(groupKeys, 0, rowValues, 0, groupKeys.length);
            dataMap.put(compositeGroupKey, rowValues);
          }

          String countKey = compositeGroupKey + "|" + position;
          if (!countMap.containsKey(countKey)) {
            countMap.put(countKey, 0);
          }
          final int aggCount = countMap.get(countKey);
          countMap.put(countKey, aggCount + 1);

          // aggregation of multiple values
          rowValues[groupKeys.length + position + i] = String.valueOf(
              reduce(
                  Double.parseDouble(rowValues[groupKeys.length + position + i]),
                  Double.parseDouble(resultSet.getString(r, 0)),
                  aggCount,
                  metricFunction.getFunctionName(),
                  sourceName
              ));

          if (timestamp != null && useCentralizedCache()) {
            rowValues[rowValues.length - 1] = timestamp;
          }
        }
      }
      position++;
    }
    List<String[]> rows = new ArrayList<>();
    rows.addAll(dataMap.values());
    return rows;
  }

  private static boolean useCentralizedCache() {
    // TODO fix caching
    return false; // CacheConfig.getInstance().useCentralizedCache();
  }

  public static double reduce(double aggregate, double value, int prevCount,
      MetricAggFunction aggFunction, String sourceName) {
    if (aggFunction.equals(MetricAggFunction.SUM)) {
      return aggregate + value;
    } else if (aggFunction.equals(MetricAggFunction.AVG) || aggFunction.isPercentile()) {
      return (aggregate * prevCount + value) / (prevCount + 1);
    } else if (aggFunction.equals(MetricAggFunction.MAX)) {
      return Math.max(aggregate, value);
    } else if (aggFunction.equals(MetricAggFunction.COUNT)) { // For all COUNT cases
      return aggregate + value;
    } else if (aggFunction.equals(MetricAggFunction.COUNT_DISTINCT)) { // For all COUNT cases
      return aggregate + value;
    } else {
      throw new IllegalArgumentException(
          String.format("Unknown aggregation function '%s'", aggFunction));
    }
  }
}
