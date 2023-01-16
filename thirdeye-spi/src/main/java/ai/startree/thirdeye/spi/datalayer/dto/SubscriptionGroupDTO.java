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

import ai.startree.thirdeye.spi.Constants.SubjectType;
import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;

public class SubscriptionGroupDTO extends AbstractDTO {

  private boolean active;
  private String name;
  private String from;
  private String cronExpression;
  private String yaml;
  private String type;

  /**
   * Intended to replace notificationSchemes.
   * Contains configs for all notification specs within the subscription group
   */
  private List<NotificationSpecDTO> specs;
  private List<AlertAssociationDto> alertAssociations;

  @Deprecated
  private NotificationSchemesDto notificationSchemes;

  private Map<String, Object> alertSuppressors;
  private SubjectType subjectType = SubjectType.ALERT;
  private Map<Long, Long> vectorClocks;
  private Map<String, Object> properties;
  private Map<String, String> refLinks;
  private List<String> owners;

  public boolean isActive() {
    return active;
  }

  public SubscriptionGroupDTO setActive(final boolean active) {
    this.active = active;
    return this;
  }

  public String getName() {
    return name;
  }

  public SubscriptionGroupDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public SubscriptionGroupDTO setFrom(final String from) {
    this.from = from;
    return this;
  }

  public String getCronExpression() {
    return cronExpression;
  }

  public SubscriptionGroupDTO setCronExpression(final String cronExpression) {
    this.cronExpression = cronExpression;
    return this;
  }

  public String getYaml() {
    return yaml;
  }

  public SubscriptionGroupDTO setYaml(final String yaml) {
    this.yaml = yaml;
    return this;
  }

  public String getType() {
    return type;
  }

  public SubscriptionGroupDTO setType(final String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getAlertSuppressors() {
    return alertSuppressors;
  }

  public SubscriptionGroupDTO setAlertSuppressors(
      final Map<String, Object> alertSuppressors) {
    this.alertSuppressors = alertSuppressors;
    return this;
  }

  public SubjectType getSubjectType() {
    return subjectType;
  }

  public SubscriptionGroupDTO setSubjectType(
      final SubjectType subjectType) {
    this.subjectType = subjectType;
    return this;
  }

  public Map<Long, Long> getVectorClocks() {
    return vectorClocks;
  }

  public SubscriptionGroupDTO setVectorClocks(
      final Map<Long, Long> vectorClocks) {
    this.vectorClocks = vectorClocks;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public SubscriptionGroupDTO setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public Map<String, String> getRefLinks() {
    return refLinks;
  }

  public SubscriptionGroupDTO setRefLinks(
      final Map<String, String> refLinks) {
    this.refLinks = refLinks;
    return this;
  }

  public List<String> getOwners() {
    return owners;
  }

  public SubscriptionGroupDTO setOwners(final List<String> owners) {
    this.owners = owners;
    return this;
  }

  public List<NotificationSpecDTO> getSpecs() {
    return specs;
  }

  public SubscriptionGroupDTO setSpecs(final List<NotificationSpecDTO> specs) {
    this.specs = specs;
    return this;
  }

  public List<AlertAssociationDto> getAlertAssociations() {
    return alertAssociations;
  }

  public SubscriptionGroupDTO setAlertAssociations(
      final List<AlertAssociationDto> alertAssociations) {
    this.alertAssociations = alertAssociations;
    return this;
  }

  public NotificationSchemesDto getNotificationSchemes() {
    return notificationSchemes;
  }

  public SubscriptionGroupDTO setNotificationSchemes(
      final NotificationSchemesDto notificationSchemes) {
    this.notificationSchemes = notificationSchemes;
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
    final SubscriptionGroupDTO that = (SubscriptionGroupDTO) o;
    return active == that.active && Objects.equal(name, that.name)
        && Objects.equal(from, that.from)
        && Objects.equal(cronExpression, that.cronExpression)
        && Objects.equal(yaml, that.yaml)
        && Objects.equal(type, that.type)
        && Objects.equal(notificationSchemes, that.notificationSchemes)
        && Objects.equal(alertSuppressors, that.alertSuppressors)
        && subjectType == that.subjectType && Objects.equal(vectorClocks,
        that.vectorClocks) && Objects.equal(properties, that.properties)
        && Objects.equal(refLinks, that.refLinks)
        && Objects.equal(owners, that.owners);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        active,
        name,
        from,
        cronExpression,
        yaml,
        type,
        notificationSchemes,
        alertSuppressors,
        subjectType,
        vectorClocks,
        properties,
        refLinks,
        owners);
  }
}
