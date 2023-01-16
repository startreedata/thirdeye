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
package ai.startree.thirdeye.plugins.notification.email;

public class EmailSendgridConfiguration {

  private String apiKey;
  private EmailRecipientsConfiguration emailRecipients;

  public String getApiKey() {
    return apiKey;
  }

  public EmailSendgridConfiguration setApiKey(final String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  public EmailRecipientsConfiguration getEmailRecipients() {
    return emailRecipients;
  }

  public EmailSendgridConfiguration setEmailRecipients(
      final EmailRecipientsConfiguration emailRecipients) {
    this.emailRecipients = emailRecipients;
    return this;
  }
}
