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
package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class EnumerationItemDTO extends AbstractDTO {

  private String name;
  private String description;
  private Map<String, Object> params;
  private AlertDTO alert;

  public String getName() {
    return name;
  }

  public EnumerationItemDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public EnumerationItemDTO setDescription(final String description) {
    this.description = description;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public EnumerationItemDTO setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public AlertDTO getAlert() {
    return alert;
  }

  public EnumerationItemDTO setAlert(final AlertDTO alert) {
    this.alert = alert;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", EnumerationItemDTO.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("description='" + description + "'")
        .add("params=" + params)
        .toString();
  }
}
