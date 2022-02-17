/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

public class MetricExpressionTest {

  @Test
  public void testMathExpression() throws Exception {
    String expressionString = "(successCount)/(__COUNT)";
    Map<String, Double> metricValueContext = new HashMap<>();
    metricValueContext.put("__COUNT", 0d);
    metricValueContext.put("successCount", 1d);

    double result = MetricExpression.evaluateExpression(expressionString, metricValueContext);
    assertThat(Double.isInfinite(result)).isTrue();
  }
}
