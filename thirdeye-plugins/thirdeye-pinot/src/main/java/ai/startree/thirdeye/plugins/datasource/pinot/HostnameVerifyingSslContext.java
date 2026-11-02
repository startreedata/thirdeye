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

import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * An {@link SSLContext} that delegates to another context, but whose {@link SSLEngine}s always
 * verify the server hostname (endpoint identification algorithm {@code HTTPS}).
 *
 * <p>The Pinot java client builds its broker {@link SSLEngine}s directly from the
 * {@link SSLContext} it is given and does not enable hostname verification on them, so the
 * context passed to the client must enforce it. Trust and key material are those of the
 * delegate. Engines created without a peer host fail the handshake, because there is no hostname
 * to verify against.
 *
 * <p>Only {@link SSLEngine}s and the default/supported {@link SSLParameters} carry the hostname
 * verification. Socket factories are the delegate's and do not verify the hostname. The wrapper
 * cannot be (re-)initialized: initialize the delegate instead.
 */
public class HostnameVerifyingSslContext extends SSLContext {

  public HostnameVerifyingSslContext(final SSLContext delegate) {
    super(new Spi(delegate), delegate.getProvider(), delegate.getProtocol());
  }

  private static SSLEngine withHostnameVerification(final SSLEngine engine) {
    engine.setSSLParameters(withHostnameVerification(engine.getSSLParameters()));
    return engine;
  }

  private static SSLParameters withHostnameVerification(final SSLParameters params) {
    params.setEndpointIdentificationAlgorithm("HTTPS");
    return params;
  }

  private static class Spi extends SSLContextSpi {

    private final SSLContext delegate;

    private Spi(final SSLContext delegate) {
      this.delegate = delegate;
    }

    @Override
    protected void engineInit(final KeyManager[] km, final TrustManager[] tm,
        final SecureRandom sr) throws KeyManagementException {
      throw new UnsupportedOperationException("Initialize the delegate SSLContext instead");
    }

    @Override
    protected SSLSocketFactory engineGetSocketFactory() {
      return delegate.getSocketFactory();
    }

    @Override
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
      return delegate.getServerSocketFactory();
    }

    @Override
    protected SSLEngine engineCreateSSLEngine() {
      return withHostnameVerification(delegate.createSSLEngine());
    }

    @Override
    protected SSLEngine engineCreateSSLEngine(final String host, final int port) {
      return withHostnameVerification(delegate.createSSLEngine(host, port));
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
      return delegate.getServerSessionContext();
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
      return delegate.getClientSessionContext();
    }

    @Override
    protected SSLParameters engineGetDefaultSSLParameters() {
      return withHostnameVerification(delegate.getDefaultSSLParameters());
    }

    @Override
    protected SSLParameters engineGetSupportedSSLParameters() {
      return withHostnameVerification(delegate.getSupportedSSLParameters());
    }
  }
}
