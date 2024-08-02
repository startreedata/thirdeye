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
package ai.startree.thirdeye.auth;

import java.util.List;
import java.util.Map;

/**
 * Plugin specific configuration is provided as plugin name -> properties.
 * Example server.yaml:
 * ---
 * accessControl:
 *   enabled: true
 *   plugins:
 *     my-plugin:
 *       prop1: val1
 * ...
 */
public class AccessControlConfiguration {

  private boolean enabled;
  private Map<String, Map<String, Object>> plugins;

  // for dev and test - allows to hardcode a map of username --> list of namespaces
  // it is used even if AccessControl.enabled is false
  private Map<String, List<String>> staticNameToNamespaces;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, Map<String, Object>> getPlugins() {
    return plugins;
  }

  public void setPlugins(final Map<String, Map<String, Object>> plugins) {
    this.plugins = plugins;
  }

  public Map<String, List<String>> getStaticNameToNamespaces() {
    return staticNameToNamespaces;
  }

  public AccessControlConfiguration setStaticNameToNamespaces(
      final Map<String, List<String>> staticNameToNamespaces) {
    this.staticNameToNamespaces = staticNameToNamespaces;
    return this;
  }
}
