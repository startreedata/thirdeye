/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.config;

import java.util.Map;

public class UiConfiguration {

  private String externalUrl;
  private String clientId;

  private HeapConfiguration heap;
  private SentryConfiguration sentry;

  private Map<String, Object> intercom;

  public String getExternalUrl() {
    return externalUrl;
  }

  public UiConfiguration setExternalUrl(final String externalUrl) {
    this.externalUrl = externalUrl;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public UiConfiguration setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public HeapConfiguration getHeap() {
    return heap;
  }

  public UiConfiguration setHeap(HeapConfiguration heap) {
    this.heap = heap;
    return this;
  }

  public SentryConfiguration getSentry() {
    return sentry;
  }

  public UiConfiguration setSentry(SentryConfiguration sentry) {
    this.sentry = sentry;
    return this;
  }

  public Map<String, Object> getIntercom() {
    return intercom;
  }

  public UiConfiguration setIntercom(final Map<String, Object> intercom) {
    this.intercom = intercom;
    return this;
  }

  public static class HeapConfiguration {

    private String environmentId;

    public String getEnvironmentId() {
      return environmentId;
    }

    public HeapConfiguration setEnvironmentId(final String environmentId) {
      this.environmentId = environmentId;
      return this;
    }
  }

  public static class SentryConfiguration {

    private String clientDsn;

    public String getClientDsn() {
      return clientDsn;
    }

    public SentryConfiguration setClientDsn(final String clientDsn) {
      this.clientDsn = clientDsn;
      return this;
    }
  }
}
