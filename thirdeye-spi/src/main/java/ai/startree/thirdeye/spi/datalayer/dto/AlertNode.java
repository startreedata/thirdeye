/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class AlertNode {

  private String name;
  private AlertNodeType type;
  private String subType;
  private MetricConfigDTO metric;
  private Map<String, Object> params;
  private List<String> dependsOn;

  public String getName() {
    return name;
  }

  public AlertNode setName(final String name) {
    this.name = name;
    return this;
  }

  public AlertNodeType getType() {
    return type;
  }

  public AlertNode setType(final AlertNodeType type) {
    this.type = type;
    return this;
  }

  public String getSubType() {
    return subType;
  }

  public AlertNode setSubType(final String subType) {
    this.subType = subType;
    return this;
  }

  public MetricConfigDTO getMetric() {
    return metric;
  }

  public AlertNode setMetric(final MetricConfigDTO metric) {
    this.metric = metric;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public AlertNode setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public List<String> getDependsOn() {
    return dependsOn;
  }

  public AlertNode setDependsOn(final List<String> dependsOn) {
    this.dependsOn = dependsOn;
    return this;
  }
}
