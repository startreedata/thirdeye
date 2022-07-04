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
package ai.startree.thirdeye.spi.rca;

import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import java.util.Map;

public class ContributorsFinderContext {

  private AggregationLoader aggregationLoader;

  private Map<String, Object> params;

  public Map<String, Object> getParams() {
    return params;
  }

  public ContributorsFinderContext setParams(
      final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public AggregationLoader getAggregationLoader() {
    return aggregationLoader;
  }

  public ContributorsFinderContext setAggregationLoader(
      final AggregationLoader aggregationLoader) {
    this.aggregationLoader = aggregationLoader;
    return this;
  }
}
