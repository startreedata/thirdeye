/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

public class EmailSendgridConfiguration {

  private String apiKey;
  private EmailRecipientsConfiguration emailRecipients;

  public String getApiKey() {
    return apiKey;
  }

  public EmailSendgridConfiguration setApiKey(final String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  public EmailRecipientsConfiguration getEmailRecipients() {
    return emailRecipients;
  }

  public EmailSendgridConfiguration setEmailRecipients(
      final EmailRecipientsConfiguration emailRecipients) {
    this.emailRecipients = emailRecipients;
    return this;
  }
}
