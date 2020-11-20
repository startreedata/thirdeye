package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class AlertApi implements ThirdEyeApi {

  private Long id;
  private String name;
  private String description;
  private String cron;
  private Date lastTimestamp;
  private Boolean active;
  private Date created;
  private Date updated;
  private UserApi owner;
  private Map<String, AlertNodeApi> nodes;
  private List<SubscriptionGroupApi> subscriptionGroups;

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

  public Map<String, AlertNodeApi> getNodes() {
    return nodes;
  }

  public AlertApi setNodes(
      final Map<String, AlertNodeApi> nodes) {
    this.nodes = nodes;
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
}
