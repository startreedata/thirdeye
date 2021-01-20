package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class NotificationSchemesApi {

  private EmailSchemeApi email;

  public EmailSchemeApi getEmail() {
    return email;
  }

  public NotificationSchemesApi setEmail(final EmailSchemeApi email) {
    this.email = email;
    return this;
  }
}
