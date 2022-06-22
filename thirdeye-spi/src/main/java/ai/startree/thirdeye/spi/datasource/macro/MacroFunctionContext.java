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

import java.util.Map;
import java.util.function.Function;
import org.joda.time.Interval;

public class MacroFunctionContext {

  private SqlExpressionBuilder sqlExpressionBuilder;
  private Interval detectionInterval;
  private Map<String, String> properties;
  /**
   * Used by macro function to remove quotes from string literal parameters
   * The macro function knows if a parameter is an identifier or a string literal.
   * The sql dialect knows how to remove quote characters from a literal.
   */
  private Function<String, String> literalUnquoter;

  public SqlExpressionBuilder getSqlExpressionBuilder() {
    return sqlExpressionBuilder;
  }

  public MacroFunctionContext setSqlExpressionBuilder(
      final SqlExpressionBuilder sqlExpressionBuilder) {
    this.sqlExpressionBuilder = sqlExpressionBuilder;
    return this;
  }

  public Interval getDetectionInterval() {
    return detectionInterval;
  }

  public MacroFunctionContext setDetectionInterval(final Interval detectionInterval) {
    this.detectionInterval = detectionInterval;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public MacroFunctionContext setProperties(
      final Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public Function<String, String> getLiteralUnquoter() {
    return literalUnquoter;
  }

  public MacroFunctionContext setLiteralUnquoter(
      final Function<String, String> literalUnquoter) {
    this.literalUnquoter = literalUnquoter;
    return this;
  }
}
