package org.apache.pinot.thirdeye;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.dropwizard.auth.Authenticator;
import org.apache.pinot.thirdeye.auth.JwtConfiguration;
import org.apache.pinot.thirdeye.auth.ThirdEyeAuthenticatorDisabled;
import org.apache.pinot.thirdeye.auth.ThirdEyeCredentials;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.ThirdEyePersistenceModule;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeServerModule extends AbstractModule {

  private final ThirdEyeServerConfiguration configuration;
  private final DataSource dataSource;

  public ThirdEyeServerModule(final ThirdEyeServerConfiguration configuration,
      final DataSource dataSource, final MetricRegistry metrics) {
    this.configuration = configuration;
    this.dataSource = dataSource;
  }

  @Override
  protected void configure() {
    install(new ThirdEyePersistenceModule(dataSource));

    bind(new TypeLiteral<Authenticator<ThirdEyeCredentials, ThirdEyePrincipal>>(){})
        .to(ThirdEyeAuthenticatorDisabled.class)
        .in(Scopes.SINGLETON);
  }

  @Singleton
  @Provides
  public JwtConfiguration getJwtConfiguration() {
    return configuration.getAuthConfiguration().getJwtConfiguration();
  }
}
