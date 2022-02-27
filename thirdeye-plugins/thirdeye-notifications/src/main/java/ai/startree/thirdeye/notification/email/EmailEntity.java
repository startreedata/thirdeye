/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

public class EmailEntity {

  private String from;
  private EmailRecipientsConfiguration recipients;
  private String subject;
  private String htmlContent;

  public String getFrom() {
    return from;
  }

  public EmailEntity setFrom(final String from) {
    this.from = from;
    return this;
  }

  public EmailRecipientsConfiguration getRecipients() {
    return recipients;
  }

  public EmailEntity setRecipients(
      final EmailRecipientsConfiguration recipients) {
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
