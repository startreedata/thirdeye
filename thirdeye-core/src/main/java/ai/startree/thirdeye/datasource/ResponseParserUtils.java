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
package ai.startree.thirdeye.datasource;

import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseParserUtils {

  public static final Logger LOGGER = LoggerFactory.getLogger(ResponseParserUtils.class);

  public static String TIME_DIMENSION_JOINER_ESCAPED = "\\|";
  public static String TIME_DIMENSION_JOINER = "|";
  public static String OTHER = "OTHER";
  public static String UNKNOWN = "UNKNOWN";

  public static Map<String, ThirdEyeResponseRow> createResponseMapByTimeAndDimension(
      ThirdEyeResponse thirdEyeResponse) {
    Map<String, ThirdEyeResponseRow> responseMap = new HashMap<>();
    int numRows = thirdEyeResponse.getNumRows();
    for (int i = 0; i < numRows; i++) {
      ThirdEyeResponseRow thirdEyeResponseRow = thirdEyeResponse.getRow(i);
      String key =
          computeTimeDimensionValues(thirdEyeResponseRow.getTimeBucketId(),
              thirdEyeResponseRow.getDimensions());
      responseMap.put(key, thirdEyeResponseRow);
    }
    return responseMap;
  }

  public static Map<String, ThirdEyeResponseRow> createResponseMapByTime(
      ThirdEyeResponse thirdEyeResponse) {
    Map<String, ThirdEyeResponseRow> responseMap;
    responseMap = new HashMap<>();
    int numRows = thirdEyeResponse.getNumRows();
    for (int i = 0; i < numRows; i++) {
      ThirdEyeResponseRow thirdEyeResponseRow = thirdEyeResponse.getRow(i);
      responseMap.put(String.valueOf(thirdEyeResponseRow.getTimeBucketId()), thirdEyeResponseRow);
    }
    return responseMap;
  }

  public static String computeTimeDimensionValue(int timeBucketId, String dimensionValue) {
    return timeBucketId + TIME_DIMENSION_JOINER + dimensionValue;
  }

  public static String computeTimeDimensionValues(int timeBucketId, List<String> dimensionValues) {
    if (dimensionValues == null || dimensionValues.size() == 0) {
      return Integer.toString(timeBucketId);
    } else if (dimensionValues.size() == 1) {
      return computeTimeDimensionValue(timeBucketId, dimensionValues.get(0));
    } else {
      StringBuilder sb = new StringBuilder(Integer.toString(timeBucketId))
          .append(TIME_DIMENSION_JOINER);
      String separator = "";
      for (String dimensionValue : dimensionValues) {
        sb.append(separator).append(dimensionValue);
        separator = TIME_DIMENSION_JOINER;
      }
      return sb.toString();
    }
  }

  public static String extractFirstDimensionValue(String timeDimensionValue) {
    String[] tokens = timeDimensionValue.split(TIME_DIMENSION_JOINER_ESCAPED);
    String dimensionValue = tokens.length < 2 ? "" : tokens[1];
    return dimensionValue;
  }

  public static List<String> extractDimensionValues(String timeDimensionValues) {
    String[] tokens = timeDimensionValues.split(TIME_DIMENSION_JOINER_ESCAPED);
    if (tokens.length < 2) {
      return Collections.emptyList();
    } else {
      List<String> res = new ArrayList<>(tokens.length - 1);
      for (int i = 1; i < tokens.length; ++i) {
        res.add(tokens[i]);
      }
      return res;
    }
  }
}
