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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertDTO extends AbstractDTO implements Schedulable {

  private String name;
  private String description;
  private boolean active;
  private String cron;
  private long lastTimestamp;
  private boolean isDataAvailabilitySchedule;
  private long taskTriggerFallBackTimeInSec;
  private List<String> owners;
  private Map<String, Object> properties;

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

  @Override
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

  @Override
  public boolean isActive() {
    return active;
  }

  public AlertDTO setActive(final boolean active) {
    this.active = active;
    return this;
  }

  public boolean isDataAvailabilitySchedule() {
    return isDataAvailabilitySchedule;
  }

  public void setDataAvailabilitySchedule(boolean dataAvailabilitySchedule) {
    isDataAvailabilitySchedule = dataAvailabilitySchedule;
  }

  public long getTaskTriggerFallBackTimeInSec() {
    return taskTriggerFallBackTimeInSec;
  }

  public void setTaskTriggerFallBackTimeInSec(long taskTriggerFallBackTimeInSec) {
    this.taskTriggerFallBackTimeInSec = taskTriggerFallBackTimeInSec;
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

  public Map<String, BaseComponent> getComponents() {
    return components;
  }

  public AlertDTO setComponents(final Map<String, BaseComponent> components) {
    this.components = components;
    return this;
  }
  //fixme make sure equals and hashCode implems are correct
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AlertDTO)) {
      return false;
    }
    AlertDTO that = (AlertDTO) o;
    return lastTimestamp == that.lastTimestamp
        && active == that.active
        && Objects.equals(cron, that.cron)
        && Objects.equals(name, that.name)
        && Objects.equals(properties, that.properties)
        && Objects.equals(isDataAvailabilitySchedule, that.isDataAvailabilitySchedule)
        && Objects.equals(taskTriggerFallBackTimeInSec, that.taskTriggerFallBackTimeInSec)
        ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cron, name, lastTimestamp, properties, active);
  }

  @Override
  public AlertDTO setCreateTime(final Timestamp createTime) {
    super.setCreateTime(createTime);
    return this;
  }
}
