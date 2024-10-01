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
package ai.startree.thirdeye.spi.config;

import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import com.google.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceServerConfigurationManager {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceServerConfigurationManager.class);

  private final NamespaceConfigurationManager namespaceConfigurationManager;

  @Inject
  public NamespaceServerConfigurationManager(
      final NamespaceConfigurationManager namespaceConfigurationManager) {
    this.namespaceConfigurationManager = namespaceConfigurationManager;
  }

  public @NonNull NamespaceConfigurationDTO currentNamespaceServerConfig(final String namespace) {
    return namespaceConfigurationManager.onboardNamespaceConfiguration(namespace);
  }
}
