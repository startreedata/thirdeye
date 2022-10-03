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

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceUtils.buildConfig;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.auto.onboard.ThirdEyePinotClient;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.Map;
import javax.inject.Singleton;
import org.apache.pinot.client.PinotConnectionBuilder;

public class PinotThirdEyeDataSourceModule extends AbstractModule {

  private final ThirdEyeDataSourceContext context;

  public PinotThirdEyeDataSourceModule(final ThirdEyeDataSourceContext context) {
    this.context = context;
  }

  @Override
  protected void configure() {
    bind(ThirdEyeDataSourceContext.class).toInstance(context);
    bind(MetricConfigManager.class).toInstance(context.getMetricConfigManager());
    bind(DatasetConfigManager.class).toInstance(context.getDatasetConfigManager());
    bind(SqlExpressionBuilder.class).to(PinotSqlExpressionBuilder.class);
    bind(SqlLanguage.class).to(PinotSqlLanguage.class);
  }

  @Singleton
  @Provides
  public PinotConnectionManager getPinotConnectionManager(
      final PinotThirdEyeDataSourceConfig config) {
    return new PinotConnectionManager(new PinotConnectionBuilder(),
        config,
        new PinotOauthTokenSupplier(config));
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

  @Singleton
  @Provides
  public ThirdEyePinotClient getThirdEyePinotClient(final ThirdEyeDataSourceContext context) {
    return new ThirdEyePinotClient(new DataSourceMetaBean()
        .setProperties(context.getDataSourceDTO().getProperties()), "pinot");
  }
}
