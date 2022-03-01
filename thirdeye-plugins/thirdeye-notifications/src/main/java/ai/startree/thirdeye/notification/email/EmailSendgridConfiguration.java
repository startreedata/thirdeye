/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

public class EmailSendgridConfiguration {

  private EmailRecipientsConfiguration emailRecipients;

  public EmailRecipientsConfiguration getEmailRecipients() {
    return emailRecipients;
  }

  public EmailSendgridConfiguration setEmailRecipients(
      final EmailRecipientsConfiguration emailRecipients) {
    this.emailRecipients = emailRecipients;
    return this;
  }
}
