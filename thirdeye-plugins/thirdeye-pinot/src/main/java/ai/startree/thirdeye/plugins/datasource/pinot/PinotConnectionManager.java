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

package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceUtils.cloneConfig;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.util.Pair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotConnectionManager {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionManager.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final PinotThirdEyeDataSourceConfig config;
  private final Supplier<String> tokenSupplier;
  private final PinotConnectionBuilder pinotConnectionBuilder;
  private Connection connection;
  private String prevToken;

  public PinotConnectionManager(final PinotConnectionBuilder pinotConnectionBuilder,
      final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
    tokenSupplier = getTokenSupplier(config.getOauth());
    this.pinotConnectionBuilder = pinotConnectionBuilder;
  }

  private Supplier<String> getTokenSupplier(final PinotOauthConfiguration oauthConfiguration) {
    if (oauthConfiguration != null && oauthConfiguration.isEnabled()) {
      /* Raise error if there is already an existing Authorization header configured */
      checkArgument(config.getHeaders() == null
              || !config.getHeaders().containsKey(AUTHORIZATION_HEADER),
          "'Authorization' header is already provided. Cannot proceed with oauth. Please remove 'Authorization' header from 'headers'");

      return () -> getOauthToken(oauthConfiguration);
    }
    return null;
  }

  private String getOauthToken(final PinotOauthConfiguration oauthConfiguration) {
    final String tokenFilePath = requireNonNull(oauthConfiguration.getTokenFilePath(),
        "tokenFilePath is null");

    final Path path = Paths.get(tokenFilePath);
    checkArgument(Files.exists(path), "tokenFile does not exist! expecting a text file.");
    checkArgument(!Files.isDirectory(path), "tokenFile is a directory! expecting a text file.");

    try {
      final String token = requireNonNull(Files.readString(path), "token is null").strip();
      return String.format("Bearer %s", token);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isNewConnectionReqd() {
    if (connection == null) {
      return true;
    }
    if (tokenSupplier == null) {
      /* no oauth. no connection update required */
      return false;
    }

    /* oauth case */
    if (prevToken == null) {
      /* no existing token to compare*/
      return true;
    }
    final String newToken = requireNonNull(tokenSupplier.get(), "token supplied is null");
    return !prevToken.equals(newToken);
  }

  public Connection get() {
    if (isNewConnectionReqd()) {
      /* Closing old connection is a lower priority. do it async */
      closeConnectionAsync(connection);

      final var p = createConnection();
      connection = p.getSecond();
      prevToken = optional(p.getFirst().getHeaders())
          .map(headers -> headers.get(AUTHORIZATION_HEADER))
          .orElse(null);
    }
    return connection;
  }

  private Pair<PinotThirdEyeDataSourceConfig, Connection> createConnection() {
    final var c = newConfig();
    return new Pair<>(c, pinotConnectionBuilder.createConnection(c));
  }

  private PinotThirdEyeDataSourceConfig newConfig() {
    if (tokenSupplier == null) {
      /* if oauth is disabled. no refresh of connections is needed */
      return config;
    }
    return newConfigWithOauthHeader();
  }

  private PinotThirdEyeDataSourceConfig newConfigWithOauthHeader() {
    final var newConfig = cloneConfig(config);

    /* Inject the oauth header into headers */
    final String newToken = tokenSupplier.get();
    if (newConfig.getHeaders() == null) {
      newConfig.setHeaders(new HashMap<>());
    }
    newConfig
        .getHeaders()
        .put(AUTHORIZATION_HEADER, newToken);
    return newConfig;
  }

  public void close() {
    closeConnection(connection);
    connection = null;
  }

  private void closeConnectionAsync(@Nullable final Connection connection) {
    if (connection != null) {
      executorService.submit(() -> closeConnection(connection));
    }
  }
  private void closeConnection(@Nullable final Connection connection) {
    try {
      final long start = System.nanoTime();
      if (connection != null) {
        connection.close();
        LOG.info(String.format("Successfully closed pinot connection. took %.2fms",
            ((System.nanoTime() - start) / 1e6)));
      }
    } catch (final Exception e) {
      LOG.error("Exception closing connection", e);
    }
  }
}
