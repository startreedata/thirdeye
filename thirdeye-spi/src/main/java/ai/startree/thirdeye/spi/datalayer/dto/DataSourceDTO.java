/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This class defines the config of a single datasource used in thirdeye
 * Eg: PinotThirdeyeDataSource
 */
public class DataSourceDTO extends AbstractDTO {

  private String name;
  private String type;
  private Map<String, Object> properties = new HashMap<>();
  private List<DataSourceMetaBean> metaList = new ArrayList<>();

  public String getName() {
    return name;
  }

  public DataSourceDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public DataSourceDTO setType(final String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public DataSourceDTO setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public List<DataSourceMetaBean> getMetaList() {
    return metaList;
  }

  public DataSourceDTO setMetaList(
      final List<DataSourceMetaBean> metaList) {
    this.metaList = metaList;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
