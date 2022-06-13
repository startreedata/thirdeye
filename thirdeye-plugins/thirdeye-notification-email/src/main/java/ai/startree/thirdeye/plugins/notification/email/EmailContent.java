/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.notification.email;

public class EmailContent {

  private String subject;
  private String htmlBody;

  public String getSubject() {
    return subject;
  }

  public EmailContent setSubject(final String subject) {
    this.subject = subject;
    return this;
  }

  public String getHtmlBody() {
    return htmlBody;
  }

  public EmailContent setHtmlBody(final String htmlBody) {
    this.htmlBody = htmlBody;
    return this;
  }
}
