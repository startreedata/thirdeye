/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
