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

package org.apache.pinot.thirdeye.datalayer.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.pinot.thirdeye.Constants.SubjectType;

/**
 * ConfigBean holds namespaced key-value configuration values.  Values are serialized into the
 * database using the default object mapper.  ConfigBean serves as a light-weight
 * alternative to existing configuration mechanisms to (a) allow at-runtime changes to configuration
 * traditionally stored in config files, and (b) alleviate the need for introducing new bean classes
 * to handle simple configuration tasks.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectionAlertConfigBean extends AbstractBean {

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

  public DetectionAlertConfigBean setActive(final boolean active) {
    this.active = active;
    return this;
  }

  public String getName() {
    return name;
  }

  public DetectionAlertConfigBean setName(final String name) {
    this.name = name;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public DetectionAlertConfigBean setFrom(final String from) {
    this.from = from;
    return this;
  }

  public String getCronExpression() {
    return cronExpression;
  }

  public DetectionAlertConfigBean setCronExpression(final String cronExpression) {
    this.cronExpression = cronExpression;
    return this;
  }

  public String getApplication() {
    return application;
  }

  public DetectionAlertConfigBean setApplication(final String application) {
    this.application = application;
    return this;
  }

  public String getYaml() {
    return yaml;
  }

  public DetectionAlertConfigBean setYaml(final String yaml) {
    this.yaml = yaml;
    return this;
  }

  public String getType() {
    return type;
  }

  public DetectionAlertConfigBean setType(String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getAlertSchemes() {
    return alertSchemes;
  }

  public DetectionAlertConfigBean setAlertSchemes(
      final Map<String, Object> alertSchemes) {
    this.alertSchemes = alertSchemes;
    return this;
  }

  public Map<String, Object> getAlertSuppressors() {
    return alertSuppressors;
  }

  public DetectionAlertConfigBean setAlertSuppressors(
      final Map<String, Object> alertSuppressors) {
    this.alertSuppressors = alertSuppressors;
    return this;
  }

  public SubjectType getSubjectType() {
    return subjectType;
  }

  public DetectionAlertConfigBean setSubjectType(
      final SubjectType subjectType) {
    this.subjectType = subjectType;
    return this;
  }

  public Map<Long, Long> getVectorClocks() {
    return vectorClocks;
  }

  public DetectionAlertConfigBean setVectorClocks(
      final Map<Long, Long> vectorClocks) {
    this.vectorClocks = vectorClocks;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public DetectionAlertConfigBean setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public Map<String, String> getRefLinks() {
    return refLinks;
  }

  public DetectionAlertConfigBean setRefLinks(
      final Map<String, String> refLinks) {
    this.refLinks = refLinks;
    return this;
  }

  public List<String> getOwners() {
    return owners;
  }

  public DetectionAlertConfigBean setOwners(final List<String> owners) {
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
    DetectionAlertConfigBean that = (DetectionAlertConfigBean) o;
    return active == that.active && Objects.equals(name, that.name) && Objects
        .equals(from, that.from)
        && Objects.equals(cronExpression, that.cronExpression) && Objects
        .equals(application, that.application)
        && subjectType == that.subjectType && Objects.equals(vectorClocks, that.vectorClocks)
        && Objects.equals(properties, that.properties) && Objects
        .equals(alertSchemes, that.alertSchemes)
        && Objects.equals(alertSuppressors, that.alertSuppressors) && Objects
        .equals(refLinks, that.refLinks)
        && Objects.equals(yaml, that.yaml);
  }

  @Override
  public int hashCode() {
    return Objects.hash(active, name, from, cronExpression, application, subjectType, vectorClocks,
        properties,
        alertSchemes, alertSuppressors, refLinks, yaml);
  }
}
