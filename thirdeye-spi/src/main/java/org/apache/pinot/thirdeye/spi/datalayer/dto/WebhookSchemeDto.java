package org.apache.pinot.thirdeye.spi.datalayer.dto;

import com.google.common.base.Objects;

public class WebhookSchemeDto {
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
    final WebhookSchemeDto that = (WebhookSchemeDto) o;
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

  public WebhookSchemeDto setUrl(final String url) {
    this.url = url;
    return this;
  }

  public String getHashKey() {
    return hashKey;
  }

  public WebhookSchemeDto setHashKey(final String hashKey) {
    this.hashKey = hashKey;
    return this;
  }
}
