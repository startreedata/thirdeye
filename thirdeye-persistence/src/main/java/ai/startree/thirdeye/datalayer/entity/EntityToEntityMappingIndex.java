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
package ai.startree.thirdeye.datalayer.entity;

public class EntityToEntityMappingIndex extends AbstractIndexEntity {

  String fromURN;
  String toURN;
  String mappingType;

  public String getFromURN() {
    return fromURN;
  }

  public void setFromURN(String fromURN) {
    this.fromURN = fromURN;
  }

  public String getToURN() {
    return toURN;
  }

  public void setToURN(String toURN) {
    this.toURN = toURN;
  }

  public String getMappingType() {
    return mappingType;
  }

  public void setMappingType(String mappingType) {
    this.mappingType = mappingType;
  }
}
