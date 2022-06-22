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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class NotificationSchemesApi {

  private EmailSchemeApi email;
  private WebhookSchemeApi webhook;

  public WebhookSchemeApi getWebhook() {
    return webhook;
  }

  public NotificationSchemesApi setWebhook(
      final WebhookSchemeApi webhook) {
    this.webhook = webhook;
    return this;
  }

  public EmailSchemeApi getEmail() {
    return email;
  }

  public NotificationSchemesApi setEmail(final EmailSchemeApi email) {
    this.email = email;
    return this;
  }
}
