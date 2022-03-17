package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIConfigurationApi implements ThirdEyeApi {

    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public UIConfigurationApi setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
}
