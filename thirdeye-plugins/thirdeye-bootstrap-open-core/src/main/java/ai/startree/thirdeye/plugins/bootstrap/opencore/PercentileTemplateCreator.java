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
package ai.startree.thirdeye.plugins.bootstrap.opencore;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.PlanNodeApi;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import java.util.Optional;

public class PercentileTemplateCreator {

  public static final String PARAMS_QUERY_KEY = "component.query";

  public static AlertTemplateApi createPercentileVariant(final AlertTemplateApi template) {
    if (template.getNodes() == null || template.getName().contains("-percentile")) {
      // skip
      return null;
    }
    final AlertTemplateApi percentileVariant = AlertTemplateMapper.INSTANCE.deepClone(template);
    boolean didModify = false;
    for (final PlanNodeApi node : percentileVariant.getNodes()) {
      didModify = modifyNode(node) || didModify;
    }
    if (!didModify) {
      return null;
    }
    percentileVariant.getProperties().add(new TemplatePropertyMetadata()
        .setName("aggregationParameter"));
    final String variantDescription = percentileVariant.getDescription().replaceAll("Aggregation function with 1 operand.*", "Aggregation function with 2 operands: PERCENTILETDIGEST, DISTINCTCOUNTHLL,etc...");
    final String variantName = percentileVariant.getName() + "-percentile";
    return percentileVariant
        .setName(variantName)
        .setDescription(variantDescription);
  }

  // attempt to update the node to create a percentile variant, and returns true if the node was updated
  private static boolean modifyNode(final PlanNodeApi node) {
    final TemplatableMap<String, Object> params = node.getParams();
    final boolean isDataFetcherNode = node.getType().equalsIgnoreCase("DataFetcher");
    if (!isDataFetcherNode) {
      return false;
    }
    final Optional<Object> queryObj = optional(params.get(PARAMS_QUERY_KEY))
        .map(Templatable::getValue);
    if (queryObj.isEmpty()) {
      return false;
    }
    // no try catch on purpose - fail fast
    final String originalQuery = (String) queryObj.get();
    final String percentileQuery = originalQuery.replace(
        "${aggregationFunction}(${aggregationColumn})",
        "${aggregationFunction}(${aggregationColumn}, ${aggregationParameter})");
    if (percentileQuery.length() == originalQuery.length()) {
      // replacement did not happen
      return false;
    }
    params.put(PARAMS_QUERY_KEY, Templatable.of(percentileQuery));
    return true;
  }
}
