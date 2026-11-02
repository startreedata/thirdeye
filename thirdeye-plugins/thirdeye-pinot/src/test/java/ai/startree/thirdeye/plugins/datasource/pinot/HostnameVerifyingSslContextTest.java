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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static org.assertj.core.api.Assertions.assertThat;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.apache.pinot.client.SslContextProviderFactory;
import org.apache.pinot.client.TlsProtocols;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.testng.annotations.Test;

public class HostnameVerifyingSslContextTest {

  @Test
  public void testPinotClientBrokerEnginesVerifyHostname() throws Exception {
    final AsyncHttpClientConfig config = SslContextProviderFactory.create()
        .configure(Dsl.config(), new HostnameVerifyingSslContext(SSLContext.getDefault()),
            TlsProtocols.defaultProtocols(false))
        .build();

    final SSLEngine engine = config.getSslEngineFactory()
        .newSslEngine(config, "broker.example.com", 443);

    assertThat(engine.getSSLParameters().getEndpointIdentificationAlgorithm()).isEqualTo("HTTPS");
    assertThat(engine.getPeerHost()).isEqualTo("broker.example.com");
    assertThat(engine.getUseClientMode()).isTrue();
  }
}
