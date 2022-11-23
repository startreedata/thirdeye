/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AlertTemplateApi implements ThirdEyeCrudApi<AlertTemplateApi> {

  private Long id;
  private String name;
  private String description;
  private String cron;
  private Date created;
  private Date updated;
  private UserApi owner;
  private List<PlanNodeApi> nodes;
  @Deprecated // use AlertMetadataApi
  private RcaMetadataApi rca;
  private AlertMetadataApi metadata;
  @Deprecated // use propertiesMetadata
  private Map<String, @Nullable Object> defaultProperties;
  private List<TemplatePropertyMetadata> properties;

  public Long getId() {
    return id;
  }

  public AlertTemplateApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public AlertTemplateApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public AlertTemplateApi setDescription(final String description) {
    this.description = description;
    return this;
  }

  public String getCron() {
    return cron;
  }

  public AlertTemplateApi setCron(final String cron) {
    this.cron = cron;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public AlertTemplateApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public AlertTemplateApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public UserApi getOwner() {
    return owner;
  }

  public AlertTemplateApi setOwner(final UserApi owner) {
    this.owner = owner;
    return this;
  }

  public List<PlanNodeApi> getNodes() {
    return nodes;
  }

  public AlertTemplateApi setNodes(
      final List<PlanNodeApi> nodes) {
    this.nodes = nodes;
    return this;
  }

  @Deprecated
  public RcaMetadataApi getRca() {
    return rca;
  }

  @Deprecated
  public AlertTemplateApi setRca(final RcaMetadataApi rca) {
    this.rca = rca;
    return this;
  }

  public AlertMetadataApi getMetadata() {
    return metadata;
  }

  public AlertTemplateApi setMetadata(final AlertMetadataApi metadata) {
    this.metadata = metadata;
    return this;
  }

  @Deprecated
  public Map<String, Object> getDefaultProperties() {
    return defaultProperties;
  }

  @Deprecated
  public AlertTemplateApi setDefaultProperties(
      final Map<String, Object> defaultProperties) {
    this.defaultProperties = defaultProperties;
    return this;
  }

  public List<TemplatePropertyMetadata> getProperties() {
    return properties;
  }

  public AlertTemplateApi setProperties(
      final List<TemplatePropertyMetadata> properties) {
    this.properties = properties;
    return this;
  }
}
