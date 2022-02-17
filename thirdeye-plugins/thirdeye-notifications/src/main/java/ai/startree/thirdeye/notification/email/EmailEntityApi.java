/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import ai.startree.thirdeye.spi.api.EmailRecipientsApi;

public class EmailEntityApi {

  private String from;
  private EmailRecipientsApi to;
  private String subject;
  private String htmlContent;
  private String snapshotPath;

  public String getFrom() {
    return from;
  }

  public EmailEntityApi setFrom(final String from) {
    this.from = from;
    return this;
  }

  public EmailRecipientsApi getTo() {
    return to;
  }

  public EmailEntityApi setTo(
      final EmailRecipientsApi to) {
    this.to = to;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public EmailEntityApi setSubject(final String subject) {
    this.subject = subject;
    return this;
  }

  public String getHtmlContent() {
    return htmlContent;
  }

  public EmailEntityApi setHtmlContent(final String htmlContent) {
    this.htmlContent = htmlContent;
    return this;
  }

  public String getSnapshotPath() {
    return snapshotPath;
  }

  public EmailEntityApi setSnapshotPath(final String snapshotPath) {
    this.snapshotPath = snapshotPath;
    return this;
  }
}
