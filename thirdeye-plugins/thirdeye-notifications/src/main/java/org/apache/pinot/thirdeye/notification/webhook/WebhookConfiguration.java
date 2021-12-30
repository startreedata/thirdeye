package org.apache.pinot.thirdeye.notification.webhook;

import com.google.common.base.Objects;

public class WebhookConfiguration {
  private String url;
  private String hashKey;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final WebhookConfiguration that = (WebhookConfiguration) o;
    return Objects.equal(url, that.url)
        && Objects.equal(hashKey, that.hashKey);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(url, hashKey);
  }

  public String getUrl() {
    return url;
  }

  public WebhookConfiguration setUrl(final String url) {
    this.url = url;
    return this;
  }

  public String getHashKey() {
    return hashKey;
  }

  public WebhookConfiguration setHashKey(final String hashKey) {
    this.hashKey = hashKey;
    return this;
  }
}
