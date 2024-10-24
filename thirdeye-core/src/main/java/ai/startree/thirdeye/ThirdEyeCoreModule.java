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
package ai.startree.thirdeye;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.datalayer.ThirdEyePersistenceModule;
import ai.startree.thirdeye.datasource.loader.DefaultAggregationLoader;
import ai.startree.thirdeye.datasource.loader.DefaultMinMaxTimeLoader;
import ai.startree.thirdeye.rootcause.configuration.RcaConfiguration;
import ai.startree.thirdeye.spi.api.NamespaceConfigurationApi;
import ai.startree.thirdeye.spi.config.TimeConfiguration;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.util.Providers;
import java.security.Provider;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeCoreModule extends AbstractModule {
  
  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeCoreModule.class);

  private final DataSource dataSource;
  private final RcaConfiguration rcaConfiguration;
  private final UiConfiguration uiConfiguration;
  private final TimeConfiguration timeConfiguration;
  private final NamespaceConfigurationDTO defaultNamespaceConfiguration;

  public ThirdEyeCoreModule(final DataSource dataSource,
      final RcaConfiguration rcaConfiguration,
      final UiConfiguration uiConfiguration,
      final TimeConfiguration timeConfiguration,
      final NamespaceConfigurationDTO defaultNamespaceConfiguration) {
    this.dataSource = dataSource;

    this.rcaConfiguration = rcaConfiguration;
    this.uiConfiguration = uiConfiguration;
    this.timeConfiguration = timeConfiguration;
    this.defaultNamespaceConfiguration = defaultNamespaceConfiguration;
  }

  @Override
  protected void configure() {
    install(new ThirdEyePersistenceModule(dataSource));

    bind(AggregationLoader.class).to(DefaultAggregationLoader.class).in(Scopes.SINGLETON);
    bind(MinMaxTimeLoader.class).to(DefaultMinMaxTimeLoader.class).in(Scopes.SINGLETON);

    bind(RcaConfiguration.class).toInstance(rcaConfiguration);
    bind(UiConfiguration.class).toInstance(uiConfiguration);
    if (timeConfiguration != null) {
      LOG.warn("Using the time configuration. This is deprecated. Please use defaultWorkspaceConfiguration.timeConfiguration instead.");
      bind(TimeConfiguration.class).toInstance(timeConfiguration);
    } else {
      bind(TimeConfiguration.class).toProvider(Providers.of(null));
    }
    bind(NamespaceConfigurationDTO.class).toInstance(defaultNamespaceConfiguration);
  }
}
