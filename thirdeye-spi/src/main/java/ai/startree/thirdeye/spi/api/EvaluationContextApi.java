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
package ai.startree.thirdeye.spi.api;

import java.util.List;

public class EvaluationContextApi {

  /**
   * Dimension filters. Used when performing RCA.
   * Format is 'dim1=val1'.
   */
  private List<String> filters;

  public List<String> getFilters() {
    return filters;
  }

  public EvaluationContextApi setFilters(final List<String> filters) {
    this.filters = filters;
    return this;
  }
}
