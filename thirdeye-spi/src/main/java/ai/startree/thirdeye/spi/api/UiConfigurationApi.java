package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UiConfigurationApi implements ThirdEyeApi {

    private String clientId;
    private boolean authEnabled;

    public String getClientId() {
        return clientId;
    }

    public UiConfigurationApi setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

  public boolean isAuthEnabled() {
    return authEnabled;
  }

  public UiConfigurationApi setAuthEnabled(final boolean authEnabled) {
    this.authEnabled = authEnabled;
    return this;
  }
}
