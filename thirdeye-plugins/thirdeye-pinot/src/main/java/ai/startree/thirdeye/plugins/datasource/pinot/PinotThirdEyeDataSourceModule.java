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

import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpHeaders;

public class PinotThirdEyeDataSourceModule extends AbstractModule {

  private final ThirdEyeDataSourceContext context;

  public PinotThirdEyeDataSourceModule(final ThirdEyeDataSourceContext context) {
    this.context = context;
  }

  @Override
  protected void configure() {
    bind(ThirdEyeDataSourceContext.class).toInstance(context);
  }

  @Singleton
  @Provides
  public PinotThirdEyeDataSourceConfig getPinotThirdEyeDataSourceConfig(
      final ThirdEyeDataSourceContext context) {
    final DataSourceDTO dataSourceDTO = requireNonNull(context.getDataSourceDTO(),
        "data source dto is null");

    final Map<String, Object> properties = requireNonNull(dataSourceDTO.getProperties(),
        "Data source property cannot be empty.");
    requireNonNull(dataSourceDTO.getName(), "name of data source dto is null");

    /* Create config class */
    return buildConfig(properties);
  }

  private static PinotThirdEyeDataSourceConfig buildConfig(final Map<String, Object> properties) {
    final PinotThirdEyeDataSourceConfig config = VANILLA_OBJECT_MAPPER
        .convertValue(properties, PinotThirdEyeDataSourceConfig.class);

    requireNonNull(config.getControllerHost(), "Controller Host is not set.");
    checkArgument(config.getControllerPort() >= 0, "Controller Portis not set");
    requireNonNull(config.getClusterName(), "Cluster Name is not set.");
    checkArgument(Set.of(PinotThirdEyeDataSource.HTTP_SCHEME, PinotThirdEyeDataSource.HTTPS_SCHEME)
            .contains(config.getControllerConnectionScheme()),
        "Controller scheme must be  either 'http' or 'https'");
    if (config.isOAuthEnabled()) {
      /* Raise error if there is already an existing Authorization header configured */
      checkArgument(config.getHeaders() == null
              || !config.getHeaders().containsKey(HttpHeaders.AUTHORIZATION),
          "'Authorization' header is already provided. Cannot proceed with oauth. Please remove 'Authorization' header from 'headers'");
    }

    return config;
  }
}
