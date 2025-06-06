/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.spi.datalayer.dto;

import com.google.common.base.Objects;
import java.util.List;

public class EmailSchemeDto {
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final EmailSchemeDto that = (EmailSchemeDto) o;
    return Objects.equal(to, that.to) && Objects.equal(
        cc,
        that.cc) && Objects.equal(bcc, that.bcc);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(to, cc, bcc);
  }

  public List<String> getTo() {
    return to;
  }

  public EmailSchemeDto setTo(final List<String> to) {
    this.to = to;
    return this;
  }

  public List<String> getCc() {
    return cc;
  }

  public EmailSchemeDto setCc(final List<String> cc) {
    this.cc = cc;
    return this;
  }

  public List<String> getBcc() {
    return bcc;
  }

  public EmailSchemeDto setBcc(final List<String> bcc) {
    this.bcc = bcc;
    return this;
  }
}
