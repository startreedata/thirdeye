/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationConfiguration {

  private boolean useSendgridEmail = false;

  @JsonProperty("smtp")
  private SmtpConfiguration smtpConfiguration;

  public boolean isUseSendgridEmail() {
    return useSendgridEmail;
  }

  public NotificationConfiguration setUseSendgridEmail(final boolean useSendgridEmail) {
    this.useSendgridEmail = useSendgridEmail;
    return this;
  }

  public SmtpConfiguration getSmtpConfiguration() {
    return smtpConfiguration;
  }

  public NotificationConfiguration setSmtpConfiguration(
      final SmtpConfiguration smtpConfiguration) {
    this.smtpConfiguration = smtpConfiguration;
    return this;
  }
}
