/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.spi.api;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This class defines the config of a single datasource used in thirdeye
 * Eg: PinotThirdeyeDataSource
 */
public class DataSourceApi implements ThirdEyeCrudApi<DataSourceApi> {

  private Long id;
  private String name;
  private String type;
  private Map<String, Object> properties;
  private List<DataSourceMetaApi> metaList;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public DataSourceApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public DataSourceApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public DataSourceApi setType(final String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public DataSourceApi setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public List<DataSourceMetaApi> getMetaList() {
    return metaList;
  }

  public DataSourceApi setMetaList(
      final List<DataSourceMetaApi> metaList) {
    this.metaList = metaList;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
