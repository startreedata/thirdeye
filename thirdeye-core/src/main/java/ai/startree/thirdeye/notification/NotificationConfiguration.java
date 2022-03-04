/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationConfiguration {

  @JsonProperty("smtp")
  private SmtpConfiguration smtpConfiguration;

  public SmtpConfiguration getSmtpConfiguration() {
    return smtpConfiguration;
  }

  public NotificationConfiguration setSmtpConfiguration(
      final SmtpConfiguration smtpConfiguration) {
    this.smtpConfiguration = smtpConfiguration;
    return this;
  }
}
