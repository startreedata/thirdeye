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
package ai.startree.thirdeye.detectionpipeline.operator;

import java.util.Map;

public class EnumerationItem {

  private String name;
  private String description;
  private Map<String, Object> params;

  public String getName() {
    return name;
  }

  public EnumerationItem setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public EnumerationItem setDescription(final String description) {
    this.description = description;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public EnumerationItem setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }
}
