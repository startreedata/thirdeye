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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.http.HttpHeaders;

@Singleton
public class PinotOauthTokenSupplier {

  private final PinotOauthConfiguration oauthConfiguration;

  @Inject
  public PinotOauthTokenSupplier(final PinotThirdEyeDataSourceConfig config) {
    oauthConfiguration = config.getOauth();

    if (oauthConfiguration != null && oauthConfiguration.isEnabled()) {
      /* Raise error if there is already an existing Authorization header configured */
      checkArgument(config.getHeaders() == null
              || !config.getHeaders().containsKey(HttpHeaders.AUTHORIZATION),
          "'Authorization' header is already provided. Cannot proceed with oauth. Please remove 'Authorization' header from 'headers'");
    }
  }

  public Supplier<String> getTokenSupplier() {
    if (oauthConfiguration != null && oauthConfiguration.isEnabled()) {
      return this::getOauthToken;
    }
    return null;
  }

  private String getOauthToken() {
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
}
