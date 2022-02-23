/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import ai.startree.thirdeye.spi.Constants.SubjectType;
import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;

/**
 * ConfigBean holds namespaced key-value configuration values.  Values are serialized into the
 * database using the default object mapper.  ConfigBean serves as a light-weight
 * alternative to existing configuration mechanisms to
 * (a) allow at-runtime changes to configuration traditionally stored in config files, and
 * (b) alleviate the need for introducing new bean classes
 * to handle simple configuration tasks.
 */
public class SubscriptionGroupDTO extends AbstractDTO {

  boolean active;
  String name;
  String from;
  String cronExpression;
  String yaml;
  String type;

  NotificationSchemesDto notificationSchemes;
  Map<String, Object> alertSuppressors;
  SubjectType subjectType = SubjectType.ALERT;

  Map<Long, Long> vectorClocks;

  Map<String, Object> properties;

  Map<String, String> refLinks;
  List<String> owners;

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
