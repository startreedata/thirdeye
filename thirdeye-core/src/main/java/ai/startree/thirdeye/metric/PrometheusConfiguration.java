package ai.startree.thirdeye.metric;

public class PrometheusConfiguration {

  private boolean enabled = false;

  public boolean isEnabled() {
    return enabled;
  }

  public PrometheusConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }
}
