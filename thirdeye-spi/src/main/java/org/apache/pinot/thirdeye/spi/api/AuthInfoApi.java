package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class AuthInfoApi implements ThirdEyeApi {

  private String oidcIssuerUrl;
  private Map<String, Object> openidConfiguration;

  public String getOidcIssuerUrl() {
    return oidcIssuerUrl;
  }

  public AuthInfoApi setOidcIssuerUrl(final String oidcIssuerUrl) {
    this.oidcIssuerUrl = oidcIssuerUrl;
    return this;
  }

  public Map<String, Object> getOpenidConfiguration() {
    return openidConfiguration;
  }

  public AuthInfoApi setOpenidConfiguration(
    final Map<String, Object> openidConfiguration) {
    this.openidConfiguration = openidConfiguration;
    return this;
  }
}
