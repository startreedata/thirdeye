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

import ai.startree.thirdeye.spi.detection.BaseComponent;
import ai.startree.thirdeye.spi.detection.health.DetectionHealth;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertDTO extends AbstractDTO {

  private String name;
  private String description;
  private boolean active;
  private String cron;
  private long lastTimestamp;
  @Deprecated // TODO CYRIL REMOVE JULY 2024
  @JsonIgnore
  private boolean isDataAvailabilitySchedule = false;
  @Deprecated // TODO CYRIL REMOVE JULY 2024
  @JsonIgnore
  private long taskTriggerFallBackTimeInSec = 0;
  private List<String> owners;
  private Map<String, Object> properties;
  private DetectionHealth health;

  // The alert template
  private AlertTemplateDTO template;

  // Values to be plugged into the above template
  private Map<String, Object> templateProperties;

  @JsonIgnore
  private Map<String, BaseComponent> components = new HashMap<>();

  public List<String> getOwners() {
    return owners;
  }

  public void setOwners(List<String> owners) {
    this.owners = owners;
  }

  public String getName() {
    return name;
  }

  public AlertDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public AlertDTO setDescription(final String description) {
    this.description = description;
    return this;
  }

  public String getCron() {
    return cron;
  }

  public AlertDTO setCron(final String cron) {
    this.cron = cron;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public AlertDTO setProperties(final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
  public long getLastTimestamp() {
    return lastTimestamp;
  }

  public AlertDTO setLastTimestamp(long lastTimestamp) {
    this.lastTimestamp = lastTimestamp;
    return this;
  }

  public boolean isActive() {
    return active;
  }

  public AlertDTO setActive(final boolean active) {
    this.active = active;
    return this;
  }

  public DetectionHealth getHealth() {
    return health;
  }

  public AlertDTO setHealth(final DetectionHealth health) {
    this.health = health;
    return this;
  }

  public AlertTemplateDTO getTemplate() {
    return template;
  }

  public AlertDTO setTemplate(
      final AlertTemplateDTO template) {
    this.template = template;
    return this;
  }

  public Map<String, Object> getTemplateProperties() {
    return templateProperties;
  }

  public AlertDTO setTemplateProperties(
      final Map<String, Object> templateProperties) {
    this.templateProperties = templateProperties;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final AlertDTO alertDTO = (AlertDTO) o;
    return active == alertDTO.active && lastTimestamp == alertDTO.lastTimestamp
        && isDataAvailabilitySchedule == alertDTO.isDataAvailabilitySchedule
        && taskTriggerFallBackTimeInSec == alertDTO.taskTriggerFallBackTimeInSec
        && Objects.equal(name, alertDTO.name)
        && Objects.equal(description, alertDTO.description)
        && Objects.equal(cron, alertDTO.cron)
        && Objects.equal(owners, alertDTO.owners)
        && Objects.equal(properties, alertDTO.properties)
        && Objects.equal(health, alertDTO.health)
        && Objects.equal(template, alertDTO.template)
        && Objects.equal(templateProperties, alertDTO.templateProperties)
        && Objects.equal(components, alertDTO.components);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), name, description, active, cron, lastTimestamp,
        isDataAvailabilitySchedule, taskTriggerFallBackTimeInSec, owners, properties, health,
        template, templateProperties, components);
  }

  @Override
  public AlertDTO setCreateTime(final Timestamp createTime) {
    super.setCreateTime(createTime);
    return this;
  }
}
