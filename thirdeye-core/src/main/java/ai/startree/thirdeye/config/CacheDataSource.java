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
package ai.startree.thirdeye.config;

import java.util.Map;

/**
 * Config for a single centralized cache data source.
 * For example, this class could be for Couchbase, or Redis, or Cassandra, etc.
 */
@Deprecated // todo cache needs reimplementation with v2 query system
public class CacheDataSource {

  private String className;

  /**
   * settings/config for the specific data source. generic since different
   * data stores may have different authentication methods.
   */
  private Map<String, Object> config;

  // left blank
  public CacheDataSource() {
  }

  public String getClassName() {
    return className;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }
}
