/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource;

import ai.startree.thirdeye.datasource.cache.MetricDataset;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.detection.MetricAggFunction;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

/**
 * This class maintains the metric name, the metric expression composed of metric ids, and the
 * aggregation function
 * The dataset is required here, because we need to know which dataset to query in cases of count(*)
 * and select max(time)
 * For other cases, it can be derived from the metric id in the expression
 */
public class MetricExpression {
  private static final Logger LOG = LoggerFactory.getLogger(MetricExpression.class);

  private static final String COUNT_METRIC = "__COUNT";
  private static final String COUNT_METRIC_ESCAPED = "A__COUNT";

  private final String expressionName;
  private final String expression;
  private final MetricAggFunction aggFunction;
  private final String dataset;

  public MetricExpression(String expression, String dataset) {
    this(expression.replaceAll("[\\s]+", ""), expression, dataset);
  }

  public MetricExpression(String expressionName, String expression, String dataset) {
    this(expressionName, expression, MetricAggFunction.SUM, dataset);
  }

  public MetricExpression(String expressionName, String expression, MetricAggFunction aggFunction,
      String dataset) {
    this.expressionName = expressionName;
    this.expression = expression;
    this.aggFunction = aggFunction;
    this.dataset = dataset;
  }

  public static double evaluateExpression(MetricExpression expression, Map<String, Double> context)
      throws Exception {
    return evaluateExpression(expression.getExpression(), context);
  }

  public static double evaluateExpression(String expressionString, Map<String, Double> context)
      throws Exception {

    Scope scope = Scope.create();
    expressionString = expressionString.replace(COUNT_METRIC, COUNT_METRIC_ESCAPED);
    Map<String, Double> metricValueContext = context;
    if (context.containsKey(COUNT_METRIC)) {
      metricValueContext = new HashMap<>(context);
      metricValueContext.put(COUNT_METRIC_ESCAPED, context.get(COUNT_METRIC));
    }
    Expression expression = Parser.parse(expressionString, scope);
    for (String metricName : metricValueContext.keySet()) {
      Variable variable = scope.create(metricName);
      if (!metricValueContext.containsKey(metricName)) {
        throw new Exception(
            "No value set for metric:" + metricName + "  in the context:" + metricValueContext);
      }
      variable.setValue(metricValueContext.get(metricName));
    }
    return expression.evaluate();
  }

  public String getExpressionName() {
    return expressionName;
  }

  public String getExpression() {
    return expression;
  }

  public String getDataset() {
    return dataset;
  }

  @Override
  public String toString() {
    return expression;
  }

  public List<MetricFunction> computeMetricFunctions(
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry
  ) {
    try {
      Scope scope = Scope.create();
      Set<String> metricTokens = new TreeSet<>(); // can be either metric names or ids ! :-/

      // expression parser errors out on variables starting with _
      // we're replacing the __COUNT default metric, with an escaped string
      // after evaluating, we replace the escaped string back with the original
      String modifiedExpressions = expression.replace(COUNT_METRIC, COUNT_METRIC_ESCAPED);

      Parser.parse(modifiedExpressions, scope);
      metricTokens = scope.getLocalNames();

      ArrayList<MetricFunction> metricFunctions = new ArrayList<>();
      for (String metricToken : metricTokens) {
        Long metricId = null;
        MetricConfigDTO metricConfig = null;
        String metricDataset = dataset;
        if (metricToken.equals(COUNT_METRIC_ESCAPED)) {
          metricToken = COUNT_METRIC;
        } else {
          try {
            metricConfig = thirdEyeCacheRegistry
                .getMetricConfigCache()
                .get(new MetricDataset(metricToken, metricDataset));
          } catch (ExecutionException e) {
            LOG.error("Exception while fetching metric by name {} and dataset {}",
                metricToken,
                metricDataset,
                e);
          }
          metricId = metricConfig.getId();
          metricDataset = metricConfig.getDataset();
        }
        DatasetConfigDTO datasetConfig = ThirdEyeUtils.getDatasetConfigFromName(metricDataset,
            thirdEyeCacheRegistry);

        metricFunctions.add(new MetricFunction(aggFunction,
            metricToken,
            metricId,
            metricDataset,
            metricConfig,
            datasetConfig));
      }
      return metricFunctions;
    } catch (ParseException e) {
      throw new RuntimeException("Exception parsing expressionString:" + expression, e);
    }
  }
}
