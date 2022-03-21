package org.apache.pinot.thirdeye;

import static org.apache.pinot.thirdeye.auth.AuthConfiguration.ISSUER_KEY;
import static org.apache.pinot.thirdeye.auth.AuthConfiguration.ISSUER_URL_KEY;
import static org.apache.pinot.thirdeye.auth.AuthConfiguration.JWKS_KEY;
import static org.apache.pinot.thirdeye.auth.AuthConfiguration.OIDC_CONFIG_KEY;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Optional;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.auth.OAuthConfig;
import org.apache.pinot.thirdeye.auth.OidcUtils;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.events.MockEventsConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeServerModule extends AbstractModule {

  private final ThirdEyeServerConfiguration configuration;
  private final DataSource dataSource;
  private final MetricRegistry metricRegistry;

  public ThirdEyeServerModule(final ThirdEyeServerConfiguration configuration,
    final DataSource dataSource, final MetricRegistry metricRegistry) {
    this.configuration = configuration;
    this.dataSource = dataSource;
    this.metricRegistry = metricRegistry;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource, configuration));

    bind(MetricRegistry.class).toInstance(metricRegistry);
    bind(ThirdEyeServerConfiguration.class).toInstance(configuration);
  }

  @Singleton
  @Provides
  public AuthConfiguration getAuthConfiguration() {
    AuthConfiguration authConfig = configuration.getAuthConfiguration();
    if (authConfig.getInfoURL() != null && !authConfig.getInfoURL().trim().isEmpty()) {
      HashMap<String, Object> info = OidcUtils.getAuthInfo(authConfig.getInfoURL());
      if (info == null || info.get(ISSUER_URL_KEY).toString().isEmpty()) {
        authConfig.setEnabled(false);
      } else {
        HashMap<String, Object> oidcConfig = (HashMap<String, Object>) info.get(OIDC_CONFIG_KEY);
        authConfig.setEnabled(true);
        OAuthConfig oauth = authConfig.getOAuthConfig();
        Optional.ofNullable(oidcConfig.get(ISSUER_KEY))
          .ifPresent(iss -> oauth.getExactMatch().put("iss", iss.toString()));
        Optional.ofNullable(oidcConfig.get(JWKS_KEY))
          .ifPresent(jwkUrl -> oauth.setKeysUrl(jwkUrl.toString()));
      }
    }
    return authConfig;
  }

  @Singleton
  @Provides
  public MockEventsConfiguration getMockEventsLoaderConfiguration() {
    return configuration.getMockEventsConfiguration();
  }
}
