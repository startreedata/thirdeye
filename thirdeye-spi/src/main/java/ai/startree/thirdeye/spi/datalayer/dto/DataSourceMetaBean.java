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

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the config of a metadata loader used in thirdeye
 * Eg: UMPMetadataLoader
 */
public class DataSourceMetaBean {

  private String classRef;
  private Map<String, Object> properties = new HashMap<>();

  public String getClassRef() {
    return classRef;
  }

  public DataSourceMetaBean setClassRef(final String classRef) {
    this.classRef = classRef;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public DataSourceMetaBean setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
