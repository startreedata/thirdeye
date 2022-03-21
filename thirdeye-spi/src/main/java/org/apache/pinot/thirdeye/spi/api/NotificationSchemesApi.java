package org.apache.pinot.thirdeye.spi.api;

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
