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

  /**
   * Assumption:
   * There is only 1 enumerator in the DAG
   * If there is multiple: then we can have multiple enumerators and multiple contexts.
   * Not dealing with that complexity for now. That will require a more complex API.
   */
  private EnumerationItemApi enumerationItem;
  public List<String> getFilters() {
    return filters;
  }

  public EvaluationContextApi setFilters(final List<String> filters) {
    this.filters = filters;
    return this;
  }
  public EnumerationItemApi getEnumerationItem() {
    return enumerationItem;
  }

  public EvaluationContextApi setEnumerationItem(
      final EnumerationItemApi enumerationItem) {
    this.enumerationItem = enumerationItem;
    return this;
  }
}
