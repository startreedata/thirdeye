package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HealthCheckApi implements ThirdEyeApi{

  private boolean healthy;
  private String message;

  public String getMessage() {
    return message;
  }

  public HealthCheckApi setMessage(final String message) {
    this.message = message;
    return this;
  }

  public boolean isHealthy() {
    return healthy;
  }

  public HealthCheckApi setHealthy(final boolean healthy) {
    this.healthy = healthy;
    return this;
  }
}
