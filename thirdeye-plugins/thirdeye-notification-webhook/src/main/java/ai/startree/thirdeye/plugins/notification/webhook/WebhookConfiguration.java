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
package ai.startree.thirdeye.plugins.notification.webhook;

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
