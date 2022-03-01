/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

public class EmailSmtpConfiguration {

  private SmtpConfiguration smtp;
  private EmailRecipientsConfiguration emailRecipients;

  public SmtpConfiguration getSmtp() {
    return smtp;
  }

  public EmailSmtpConfiguration setSmtp(
      final SmtpConfiguration smtp) {
    this.smtp = smtp;
    return this;
  }

  public EmailRecipientsConfiguration getEmailRecipients() {
    return emailRecipients;
  }

  public EmailSmtpConfiguration setEmailRecipients(
      final EmailRecipientsConfiguration emailRecipients) {
    this.emailRecipients = emailRecipients;
    return this;
  }
}
