/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.notification.email;

import com.google.common.base.Objects;
import java.util.List;

public class EmailRecipientsConfiguration {

  private String from;
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;

  public String getFrom() {
    return from;
  }

  public EmailRecipientsConfiguration setFrom(final String from) {
    this.from = from;
    return this;
  }

  public List<String> getTo() {
    return to;
  }

  public EmailRecipientsConfiguration setTo(final List<String> to) {
    this.to = to;
    return this;
  }

  public List<String> getCc() {
    return cc;
  }

  public EmailRecipientsConfiguration setCc(final List<String> cc) {
    this.cc = cc;
    return this;
  }

  public List<String> getBcc() {
    return bcc;
  }

  public EmailRecipientsConfiguration setBcc(final List<String> bcc) {
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
    final EmailRecipientsConfiguration that = (EmailRecipientsConfiguration) o;
    return Objects.equal(to, that.to) && Objects.equal(
        cc,
        that.cc) && Objects.equal(bcc, that.bcc);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(to, cc, bcc);
  }
}
