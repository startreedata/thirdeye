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
package ai.startree.thirdeye.detectionpipeline.operator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnumeratorOperatorParams {

  private List<String> idKeys;
  private String type;

  public List<String> getIdKeys() {
    return idKeys;
  }

  public EnumeratorOperatorParams setIdKeys(final List<String> idKeys) {
    this.idKeys = idKeys;
    return this;
  }

  public String getType() {
    return type;
  }

  public EnumeratorOperatorParams setType(final String type) {
    this.type = type;
    return this;
  }
}
