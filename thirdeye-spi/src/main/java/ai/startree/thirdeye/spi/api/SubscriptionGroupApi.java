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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class SubscriptionGroupApi implements ThirdEyeCrudApi<SubscriptionGroupApi> {

  private Long id;
  private String name;
  private Boolean active;
  private String type;

  private List<AlertApi> alerts;
  private List<AlertAssociationApi> alertAssociations;
  private String cron;

  private Date created;
  private Date updated;
  private UserApi owner;

  private List<NotificationSpecApi> specs;

  @Deprecated
  private NotificationSchemesApi notificationSchemes;
  private TimeWindowSuppressorApi alertSuppressors;

  public Long getId() {
    return id;
  }

  public SubscriptionGroupApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Boolean getActive() {
    return active;
  }

  public String getType() {
    return type;
  }

  public SubscriptionGroupApi setType(String type) {
    this.type = type;
    return this;
  }

  public SubscriptionGroupApi setActive(Boolean active) {
    this.active = active;
    return this;
  }

  public SubscriptionGroupApi setName(final String name) {
    this.name = name;
    return this;
  }

  public List<AlertApi> getAlerts() {
    return alerts;
  }

  public SubscriptionGroupApi setAlerts(
      final List<AlertApi> alerts) {
    this.alerts = alerts;
    return this;
  }

  public List<AlertAssociationApi> getAlertAssociations() {
    return alertAssociations;
  }

  public SubscriptionGroupApi setAlertAssociations(
      final List<AlertAssociationApi> alertAssociations) {
    this.alertAssociations = alertAssociations;
    return this;
  }

  public String getCron() {
    return cron;
  }

  public SubscriptionGroupApi setCron(String cron) {
    this.cron = cron;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public SubscriptionGroupApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public SubscriptionGroupApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public UserApi getOwner() {
    return owner;
  }

  public SubscriptionGroupApi setOwner(final UserApi owner) {
    this.owner = owner;
    return this;
  }

  public List<NotificationSpecApi> getSpecs() {
    return specs;
  }

  public SubscriptionGroupApi setSpecs(
      final List<NotificationSpecApi> specs) {
    this.specs = specs;
    return this;
  }

  public NotificationSchemesApi getNotificationSchemes() {
    return notificationSchemes;
  }

  public SubscriptionGroupApi setNotificationSchemes(
      final NotificationSchemesApi notificationSchemes) {
    this.notificationSchemes = notificationSchemes;
    return this;
  }

  public TimeWindowSuppressorApi getAlertSuppressors() {
    return alertSuppressors;
  }

  public SubscriptionGroupApi setAlertSuppressors(TimeWindowSuppressorApi alertSuppressors) {
    this.alertSuppressors = alertSuppressors;
    return this;
  }
}
