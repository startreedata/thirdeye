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

import ai.startree.thirdeye.spi.datalayer.dto.AlertNodeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class AlertNodeApi {

  private String name;
  private AlertNodeType type;
  private String subType;
  private MetricApi metric;
  private Map<String, Object> params;
  private List<String> dependsOn;

  public String getName() {
    return name;
  }

  public AlertNodeApi setName(final String name) {
    this.name = name;
    return this;
  }

  public AlertNodeType getType() {
    return type;
  }

  public AlertNodeApi setType(final AlertNodeType type) {
    this.type = type;
    return this;
  }

  public String getSubType() {
    return subType;
  }

  public AlertNodeApi setSubType(final String subType) {
    this.subType = subType;
    return this;
  }

  public MetricApi getMetric() {
    return metric;
  }

  public AlertNodeApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public AlertNodeApi setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public List<String> getDependsOn() {
    return dependsOn;
  }

  public AlertNodeApi setDependsOn(final List<String> dependsOn) {
    this.dependsOn = dependsOn;
    return this;
  }
}
