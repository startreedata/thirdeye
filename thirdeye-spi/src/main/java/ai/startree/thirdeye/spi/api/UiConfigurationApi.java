/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UiConfigurationApi implements ThirdEyeApi {

  private String clientId;
  private boolean authEnabled;
  private HeapConfigurationApi heap;
  private SentryConfigurationApi sentry;

  private Map<String, Object> intercom;

  public String getClientId() {
    return clientId;
  }

  public UiConfigurationApi setClientId(final String clientId) {
    this.clientId = clientId;
    return this;
  }

  public boolean isAuthEnabled() {
    return authEnabled;
  }

  public UiConfigurationApi setAuthEnabled(final boolean authEnabled) {
    this.authEnabled = authEnabled;
    return this;
  }

  public HeapConfigurationApi getHeap() {
    return heap;
  }

  public UiConfigurationApi setHeap(HeapConfigurationApi heap) {
    this.heap = heap;
    return this;
  }

  public SentryConfigurationApi getSentry() {
    return sentry;
  }

  public UiConfigurationApi setSentry(SentryConfigurationApi sentry) {
    this.sentry = sentry;
    return this;
  }

  public Map<String, Object> getIntercom() {
    return intercom;
  }

  public UiConfigurationApi setIntercom(final Map<String, Object> intercom) {
    this.intercom = intercom;
    return this;
  }

  public static class HeapConfigurationApi {

    private String environmentId;

    public String getEnvironmentId() {
      return environmentId;
    }

    public HeapConfigurationApi setEnvironmentId(final String environmentId) {
      this.environmentId = environmentId;
      return this;
    }
  }

  public static class SentryConfigurationApi {

    private String clientDsn;

    public String getClientDsn() {
      return clientDsn;
    }

    public SentryConfigurationApi setClientDsn(final String clientDsn) {
      this.clientDsn = clientDsn;
      return this;
    }
  }
}
