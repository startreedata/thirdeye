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
package ai.startree.thirdeye.spi.datalayer.dto;

import ai.startree.thirdeye.spi.api.QuotasConfigurationApi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamespaceConfigurationDTO extends AbstractDTO {

  TimeConfigurationDTO timeConfiguration;
  
  private TemplateConfigurationDTO templateConfiguration;

  private QuotasConfigurationDTO quotasConfiguration;

  public TimeConfigurationDTO getTimeConfiguration() {
    return timeConfiguration;
  }

  public NamespaceConfigurationDTO setTimeConfiguration(
      final TimeConfigurationDTO timeConfiguration) {
    this.timeConfiguration = timeConfiguration;
    return this;
  }

  public TemplateConfigurationDTO getTemplateConfiguration() {
    return templateConfiguration;
  }

  public NamespaceConfigurationDTO setTemplateConfiguration(
      final TemplateConfigurationDTO templateConfiguration) {
    this.templateConfiguration = templateConfiguration;
    return this;
  }

  public QuotasConfigurationDTO getQuotasConfiguration() {
    return quotasConfiguration;
  }

  public NamespaceConfigurationDTO setQuotasConfiguration(
      final QuotasConfigurationDTO quotasConfiguration) {
    this.quotasConfiguration = quotasConfiguration;
    return this;
  }
}
