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
package ai.startree.thirdeye.spi.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AlertApi implements ThirdEyeCrudApi<AlertApi> {

  private Long id;
  private String name;
  private String description;
  private AlertTemplateApi template;
  private Map<String, Object> templateProperties;
  private String cron;
  private Date lastTimestamp;
  private Boolean active;
  private Date created;
  private Date updated;
  private UserApi owner;
  private List<SubscriptionGroupApi> subscriptionGroups;
  private AuthorizationConfigurationApi authorization;


  public Long getId() {
    return id;
  }

  public AlertApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public AlertApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public AlertApi setDescription(final String description) {
    this.description = description;
    return this;
  }

  public AlertTemplateApi getTemplate() {
    return template;
  }

  public AlertApi setTemplate(final AlertTemplateApi template) {
    this.template = template;
    return this;
  }

  public Map<String, Object> getTemplateProperties() {
    return templateProperties;
  }

  public AlertApi setTemplateProperties(
      final Map<String, Object> templateProperties) {
    this.templateProperties = templateProperties;
    return this;
  }

  public String getCron() {
    return cron;
  }

  public AlertApi setCron(final String cron) {
    this.cron = cron;
    return this;
  }

  public Date getLastTimestamp() {
    return lastTimestamp;
  }

  public AlertApi setLastTimestamp(final Date lastTimestamp) {
    this.lastTimestamp = lastTimestamp;
    return this;
  }

  public Boolean getActive() {
    return active;
  }

  public AlertApi setActive(final Boolean active) {
    this.active = active;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public AlertApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public AlertApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public UserApi getOwner() {
    return owner;
  }

  public AlertApi setOwner(final UserApi owner) {
    this.owner = owner;
    return this;
  }

  public List<SubscriptionGroupApi> getSubscriptionGroups() {
    return subscriptionGroups;
  }

  public AlertApi setSubscriptionGroups(
      final List<SubscriptionGroupApi> subscriptionGroups) {
    this.subscriptionGroups = subscriptionGroups;
    return this;
  }

  public AuthorizationConfigurationApi getAuthorization() {
    return authorization;
  }

  public AlertApi setAuthorization(final AuthorizationConfigurationApi authorization) {
    this.authorization = authorization;
    return this;
  }
}
