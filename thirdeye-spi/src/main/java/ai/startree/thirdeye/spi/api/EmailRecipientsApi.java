/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Container class for email alert recipients
 */
public class EmailRecipientsApi {

  Set<String> to;
  Set<String> cc;
  Set<String> bcc;

  public EmailRecipientsApi(Collection<String> to, Collection<String> cc,
      Collection<String> bcc) {
    this.to = new HashSet<>(to);
    this.cc = cc != null ? new HashSet<>(cc) : new HashSet<>();
    this.bcc = bcc != null ? new HashSet<>(bcc) : new HashSet<>();
  }

  public Set<String> getTo() {
    return to;
  }

  public EmailRecipientsApi setTo(Set<String> to) {
    this.to = to;
    return this;
  }

  public Set<String> getCc() {
    return cc;
  }

  public EmailRecipientsApi setCc(Set<String> cc) {
    this.cc = cc;
    return this;
  }

  public Set<String> getBcc() {
    return bcc;
  }

  public EmailRecipientsApi setBcc(Set<String> bcc) {
    this.bcc = bcc;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EmailRecipientsApi that = (EmailRecipientsApi) o;
    return Objects.equals(to, that.to) && Objects.equals(cc, that.cc) && Objects
        .equals(bcc, that.bcc);
  }

  @Override
  public int hashCode() {
    return Objects.hash(to, cc, bcc);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("TO", Arrays.toString((getTo() != null) ? getTo().toArray() : new String[]{}))
        .add("CC", Arrays.toString((getCc() != null) ? getCc().toArray() : new String[]{}))
        .add("BCC", Arrays.toString((getBcc() != null) ? getBcc().toArray() : new String[]{}))
        .toString();
  }
}
