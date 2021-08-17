package org.apache.pinot.thirdeye.spi.datalayer.dto;

import com.google.common.base.Objects;

public class NotificationSchemesDto {

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
    return Objects.equal(emailScheme, that.emailScheme);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(emailScheme);
  }

  public EmailSchemeDto getEmailScheme() {
    return emailScheme;
  }

  public NotificationSchemesDto setEmailScheme(
      final EmailSchemeDto emailScheme) {
    this.emailScheme = emailScheme;
    return this;
  }

}
