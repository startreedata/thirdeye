/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.MetricDataset;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.MetricAggFunction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

  private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static List<MetricExpression> convertToMetricExpressions(String metricsJson,
      MetricAggFunction aggFunction, String dataset,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) throws ExecutionException {

    List<MetricExpression> metricExpressions = new ArrayList<>();
    if (metricsJson == null) {
      return metricExpressions;
    }
    ArrayList<String> metricExpressionNames;
    try {
      TypeReference<ArrayList<String>> valueTypeRef = new TypeReference<>() {
      };
      metricExpressionNames = OBJECT_MAPPER.readValue(metricsJson, valueTypeRef);
    } catch (Exception e) {
      metricExpressionNames = new ArrayList<>();
      String[] metrics = metricsJson.split(",");
      for (String metric : metrics) {
        metricExpressionNames.add(metric.trim());
      }
    }
    for (String metricExpressionName : metricExpressionNames) {
      MetricConfigDTO metricConfig = thirdEyeCacheRegistry.getMetricConfigCache()
          .get(new MetricDataset(metricExpressionName, dataset));
      String derivedMetricExpression = Optional.ofNullable(metricConfig.getDerivedMetricExpression())
          .orElse(metricConfig.getName());
      MetricExpression metricExpression = new MetricExpression(metricExpressionName,
          derivedMetricExpression,
          aggFunction, dataset);
      metricExpressions.add(metricExpression);
    }
    return metricExpressions;
  }
}
