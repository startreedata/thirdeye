/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationConfiguration {

  @JsonProperty("smtp")
  private SmtpConfiguration smtpConfiguration;

  @JsonProperty("sendgrid")
  private SendgridConfiguration sendgridConfiguration;

  public SmtpConfiguration getSmtpConfiguration() {
    return smtpConfiguration;
  }

  public NotificationConfiguration setSmtpConfiguration(
      final SmtpConfiguration smtpConfiguration) {
    this.smtpConfiguration = smtpConfiguration;
    return this;
  }

  public SendgridConfiguration getSendgridConfiguration() {
    return sendgridConfiguration;
  }

  public NotificationConfiguration setSendgridConfiguration(
      final SendgridConfiguration sendgridConfiguration) {
    this.sendgridConfiguration = sendgridConfiguration;
    return this;
  }
}
