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
