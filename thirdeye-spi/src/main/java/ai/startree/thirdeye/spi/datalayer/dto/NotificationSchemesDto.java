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
package ai.startree.thirdeye.spi.datalayer.dto;

import com.google.common.base.Objects;

public class NotificationSchemesDto {

  WebhookSchemeDto webhookScheme;
  EmailSchemeDto emailScheme;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NotificationSchemesDto that = (NotificationSchemesDto) o;
    return Objects.equal(emailScheme, that.emailScheme)
        && Objects.equal(webhookScheme, that.webhookScheme);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(emailScheme, webhookScheme);
  }

  public EmailSchemeDto getEmailScheme() {
    return emailScheme;
  }

  public NotificationSchemesDto setEmailScheme(
      final EmailSchemeDto emailScheme) {
    this.emailScheme = emailScheme;
    return this;
  }

  public WebhookSchemeDto getWebhookScheme() {
    return webhookScheme;
  }

  public NotificationSchemesDto setWebhookScheme(
      final WebhookSchemeDto webhookScheme) {
    this.webhookScheme = webhookScheme;
    return this;
  }
}
