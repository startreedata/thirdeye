package org.apache.pinot.thirdeye.notification.commons;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class AlerterConfigurations {
  private SmtpConfiguration smtpConfiguration;
  private JiraConfiguration jiraConfiguration;

  public SmtpConfiguration getSmtpConfiguration() {
    return smtpConfiguration;
  }

  public AlerterConfigurations setSmtpConfiguration(
      final SmtpConfiguration smtpConfiguration) {
    this.smtpConfiguration = smtpConfiguration;
    return this;
  }

  public JiraConfiguration getJiraConfiguration() {
    return jiraConfiguration;
  }

  public AlerterConfigurations setJiraConfiguration(
      final JiraConfiguration jiraConfiguration) {
    this.jiraConfiguration = jiraConfiguration;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("smtpConfiguration", smtpConfiguration)
        .add("jiraConfiguration", jiraConfiguration)
        .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AlerterConfigurations that = (AlerterConfigurations) o;
    return Objects.equal(smtpConfiguration, that.smtpConfiguration)
        && Objects.equal(jiraConfiguration, that.jiraConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(smtpConfiguration, jiraConfiguration);
  }
}
