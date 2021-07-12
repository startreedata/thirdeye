/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.spi.datalayer.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.pinot.thirdeye.spi.Constants.SubjectType;

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
  String application;
  String yaml;
  String type;

  Map<String, Object> alertSchemes;
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

  public String getApplication() {
    return application;
  }

  public SubscriptionGroupDTO setApplication(final String application) {
    this.application = application;
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

  public Map<String, Object> getAlertSchemes() {
    return alertSchemes;
  }

  public SubscriptionGroupDTO setAlertSchemes(
      final Map<String, Object> alertSchemes) {
    this.alertSchemes = alertSchemes;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionGroupDTO that = (SubscriptionGroupDTO) o;
    if (getId() != null || that.getId() != null) {
      return super.equals(that);
    } else {
      return active == that.active && Objects.equals(name, that.name)
          && Objects.equals(from, that.from)
          && Objects.equals(cronExpression, that.cronExpression)
          && Objects.equals(application, that.application)
          && subjectType == that.subjectType
          && Objects.equals(vectorClocks, that.vectorClocks)
          && Objects.equals(properties, that.properties)
          && Objects.equals(alertSchemes, that.alertSchemes)
          && Objects.equals(alertSuppressors, that.alertSuppressors)
          && Objects.equals(refLinks, that.refLinks)
          && Objects.equals(yaml, that.yaml);

    }
  }

  @Override
  public int hashCode() {
    if (getId() != null) {
      return super.hashCode();
    }
    return Objects.hash(active,
        name,
        from,
        cronExpression,
        application,
        subjectType,
        vectorClocks,
        properties,
        alertSchemes,
        alertSuppressors,
        refLinks,
        yaml);
  }
}
