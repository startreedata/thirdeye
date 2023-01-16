/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class EmailSchemeApi {

  private String from;
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;

  public String getFrom() {
    return from;
  }

  public EmailSchemeApi setFrom(final String from) {
    this.from = from;
    return this;
  }

  public List<String> getTo() {
    return to;
  }

  public EmailSchemeApi setTo(final List<String> to) {
    this.to = to;
    return this;
  }

  public List<String> getCc() {
    return cc;
  }

  public EmailSchemeApi setCc(final List<String> cc) {
    this.cc = cc;
    return this;
  }

  public List<String> getBcc() {
    return bcc;
  }

  public EmailSchemeApi setBcc(final List<String> bcc) {
    this.bcc = bcc;
    return this;
  }
}
