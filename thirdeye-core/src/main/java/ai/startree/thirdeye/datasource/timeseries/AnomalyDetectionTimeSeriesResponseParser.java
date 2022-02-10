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

package ai.startree.thirdeye.datasource.timeseries;

import ai.startree.thirdeye.datasource.ResponseParserUtils;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnomalyDetectionTimeSeriesResponseParser extends BaseTimeSeriesResponseParser {

  private static final Logger LOG = LoggerFactory
      .getLogger(AnomalyDetectionTimeSeriesResponseParser.class);

  protected List<TimeSeriesRow> parseGroupByTimeDimensionResponse(ThirdEyeResponse response) {
    Map<String, ThirdEyeResponseRow> responseMap = ResponseParserUtils
        .createResponseMapByTimeAndDimension(response);
    List<Range<DateTime>> ranges = getTimeRanges(response.getRequest());
    int numTimeBuckets = ranges.size();
    List<MetricFunction> metricFunctions = response.getMetricFunctions();
    List<TimeSeriesRow> rows = new ArrayList<>();

    // group by time and dimension values
    Set<String> timeDimensionValues = new HashSet<>();
    timeDimensionValues.addAll(responseMap.keySet());
    Set<List<String>> dimensionValuesList = new HashSet<>();
    for (String timeDimensionValue : timeDimensionValues) {
      List<String> dimensionValues = ResponseParserUtils.extractDimensionValues(timeDimensionValue);
      dimensionValuesList.add(dimensionValues);
    }

    // group by dimension names (the 0th dimension, which is the time bucket, is skipped).
    List<String> groupKeyColumns = response.getGroupKeyColumns();
    List<String> dimensionNameList = new ArrayList<>(groupKeyColumns.size() - 1);
    for (int i = 1; i < groupKeyColumns.size(); ++i) {
      dimensionNameList.add(groupKeyColumns.get(i));
    }

    // Construct and add time series rows
    for (List<String> dimensionValues : dimensionValuesList) {
      List<TimeSeriesRow> timeSeriesRows =
          buildTimeSeriesRows(responseMap, ranges, numTimeBuckets, dimensionNameList,
              dimensionValues,
              metricFunctions);

      rows.addAll(timeSeriesRows);
    }

    return rows;
  }
}
