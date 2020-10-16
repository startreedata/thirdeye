package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class SubscriptionGroupApi {

  private Long id;
  private String name;

  private ApplicationApi application;
  private List<AlertApi> alerts;

  private Date created;
  private Date updated;
  private UserApi owner;

  private EmailSettingsApi emailSettings;

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

  public SubscriptionGroupApi setName(final String name) {
    this.name = name;
    return this;
  }

  public ApplicationApi getApplication() {
    return application;
  }

  public SubscriptionGroupApi setApplication(
      final ApplicationApi application) {
    this.application = application;
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

  public EmailSettingsApi getEmailSettings() {
    return emailSettings;
  }

  public SubscriptionGroupApi setEmailSettings(
      final EmailSettingsApi emailSettings) {
    this.emailSettings = emailSettings;
    return this;
  }
}
