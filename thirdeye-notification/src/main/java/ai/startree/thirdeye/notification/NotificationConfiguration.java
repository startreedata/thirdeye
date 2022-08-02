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
package ai.startree.thirdeye.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationConfiguration {

  private boolean useSendgridEmail = false;

  @JsonProperty("smtp")
  private SmtpConfiguration smtpConfiguration;

  public boolean isUseSendgridEmail() {
    return useSendgridEmail;
  }

  public NotificationConfiguration setUseSendgridEmail(final boolean useSendgridEmail) {
    this.useSendgridEmail = useSendgridEmail;
    return this;
  }

  public SmtpConfiguration getSmtpConfiguration() {
    return smtpConfiguration;
  }

  public NotificationConfiguration setSmtpConfiguration(
      final SmtpConfiguration smtpConfiguration) {
    this.smtpConfiguration = smtpConfiguration;
    return this;
  }
}
