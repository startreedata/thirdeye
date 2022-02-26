/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import ai.startree.thirdeye.spi.api.EmailRecipientsApi;

public class EmailEntity {

  private String from;
  private EmailRecipientsApi recipients;
  private String subject;
  private String htmlContent;
  private String snapshotPath;

  public String getFrom() {
    return from;
  }

  public EmailEntity setFrom(final String from) {
    this.from = from;
    return this;
  }

  public EmailRecipientsApi getRecipients() {
    return recipients;
  }

  public EmailEntity setRecipients(
      final EmailRecipientsApi recipients) {
    this.recipients = recipients;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public EmailEntity setSubject(final String subject) {
    this.subject = subject;
    return this;
  }

  public String getHtmlContent() {
    return htmlContent;
  }

  public EmailEntity setHtmlContent(final String htmlContent) {
    this.htmlContent = htmlContent;
    return this;
  }
}
