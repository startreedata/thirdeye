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
package org.apache.pinot.client;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.pinot.spi.utils.CommonConstants;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder;
import org.asynchttpclient.Dsl;

public class ThirdEyeJsonAsyncHttpPinotClientTransportFactory implements PinotClientTransportFactory {

  private Map<String, String> headers = new HashMap<>();
  private String scheme = CommonConstants.HTTP_PROTOCOL;
  private SSLContext sslContext = null;

  private int readTimeoutMs = 60000;
  private int connectTimeoutMs = 2000;
  private int brokerResponseTimeoutMs = 60000;

  public ThirdEyeJsonAsyncHttpPinotClientTransportFactory setHeaders(
      final Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public ThirdEyeJsonAsyncHttpPinotClientTransportFactory setScheme(final String scheme) {
    this.scheme = scheme;
    return this;
  }

  public ThirdEyeJsonAsyncHttpPinotClientTransportFactory setSslContext(
      final SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  public ThirdEyeJsonAsyncHttpPinotClientTransportFactory setReadTimeoutMs(final int readTimeoutMs) {
    this.readTimeoutMs = readTimeoutMs;
    return this;
  }

  public ThirdEyeJsonAsyncHttpPinotClientTransportFactory setConnectTimeoutMs(
      final int connectTimeoutMs) {
    this.connectTimeoutMs = connectTimeoutMs;
    return this;
  }

  public ThirdEyeJsonAsyncHttpPinotClientTransportFactory setBrokerResponseTimeoutMs(
      final int brokerResponseTimeoutMs) {
    this.brokerResponseTimeoutMs = brokerResponseTimeoutMs;
    return this;
  }

  @Override
  public PinotClientTransport buildTransport() {
    final Builder builder = Dsl.config();
    if (sslContext != null) {
      builder.setSslContext(new JdkSslContext(sslContext, true, ClientAuth.OPTIONAL));
    }

    builder
        .setReadTimeout(readTimeoutMs)
        .setConnectTimeout(connectTimeoutMs);
    final AsyncHttpClient _httpClient = Dsl.asyncHttpClient(builder.build());

    return new ForkedJsonAsyncHttpPinotClientTransport(headers,
        scheme,
        _httpClient,
        brokerResponseTimeoutMs);
  }
}
