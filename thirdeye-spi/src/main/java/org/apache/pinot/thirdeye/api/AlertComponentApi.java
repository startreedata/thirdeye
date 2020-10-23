package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.spec.AbstractSpec;

@JsonInclude(Include.NON_NULL)
public class AlertComponentApi {

  private String type;
  private MetricApi metric;
  private Map<String, Object> params;

  public String getType() {
    return type;
  }

  public AlertComponentApi setType(final String type) {
    this.type = type;
    return this;
  }

  public MetricApi getMetric() {
    return metric;
  }

  public AlertComponentApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public AlertComponentApi setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }
}
