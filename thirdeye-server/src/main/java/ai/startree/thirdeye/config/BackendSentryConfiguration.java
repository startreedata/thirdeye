/*
 * Copyright 2024 StarTree Inc
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

public class BackendSentryConfiguration {
  
  private String dsn = null;
  
  private String environment;

  private Map<String, String> tags = Map.of();

  public String getDsn() {
    return dsn;
  }

  public BackendSentryConfiguration setDsn(final String dsn) {
    this.dsn = dsn;
    return this;
  }

  public String getEnvironment() {
    return environment;
  }

  public BackendSentryConfiguration setEnvironment(final String environment) {
    this.environment = environment;
    return this;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public BackendSentryConfiguration setTags(final Map<String, String> tags) {
    this.tags = tags;
    return this;
  }
}
