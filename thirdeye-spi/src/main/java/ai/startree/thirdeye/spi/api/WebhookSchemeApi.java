/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Objects;

@JsonInclude(Include.NON_NULL)
public class WebhookSchemeApi {
  private String url;
  private String hashKey;

  public String getUrl() {
    return url;
  }

  public WebhookSchemeApi setUrl(final String url) {
    this.url = url;
    return this;
  }

  public String getHashKey() {
    return hashKey;
  }

  public WebhookSchemeApi setHashKey(final String hashKey) {
    this.hashKey = hashKey;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final WebhookSchemeApi that = (WebhookSchemeApi) o;
    return Objects.equal(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(url);
  }
}
