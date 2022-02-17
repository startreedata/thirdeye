/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RootcauseTemplateDTO extends AbstractDTO {

  String name;
  String owner;
  String application;
  long metricId;
  List<Map<String, Object>> modules;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public long getMetricId() {
    return metricId;
  }

  public void setMetricId(long metricId) {
    this.metricId = metricId;
  }

  public List<Map<String, Object>> getModules() {
    return modules;
  }

  public void setModules(List<Map<String, Object>> modules) {
    this.modules = modules;
  }
}

