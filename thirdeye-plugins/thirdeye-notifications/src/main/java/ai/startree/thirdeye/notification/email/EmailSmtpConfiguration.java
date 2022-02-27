/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import com.google.common.base.Objects;
import java.util.List;

public class EmailSmtpConfiguration {
  private SmtpConfiguration smtp;
  private String from;
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;

  public SmtpConfiguration getSmtp() {
    return smtp;
  }

  public EmailSmtpConfiguration setSmtp(
      final SmtpConfiguration smtp) {
    this.smtp = smtp;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public EmailSmtpConfiguration setFrom(final String from) {
    this.from = from;
    return this;
  }

  public List<String> getTo() {
    return to;
  }

  public EmailSmtpConfiguration setTo(final List<String> to) {
    this.to = to;
    return this;
  }

  public List<String> getCc() {
    return cc;
  }

  public EmailSmtpConfiguration setCc(final List<String> cc) {
    this.cc = cc;
    return this;
  }

  public List<String> getBcc() {
    return bcc;
  }

  public EmailSmtpConfiguration setBcc(final List<String> bcc) {
    this.bcc = bcc;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final EmailSmtpConfiguration that = (EmailSmtpConfiguration) o;
    return Objects.equal(to, that.to) && Objects.equal(
        cc,
        that.cc) && Objects.equal(bcc, that.bcc);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(to, cc, bcc);
  }
}
