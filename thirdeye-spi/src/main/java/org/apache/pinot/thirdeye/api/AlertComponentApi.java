package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class AlertComponentApi {

  private String name;
  private String type;
  private MetricApi metric;
  private Map<String, Object> params;
  private AlertApi alert;

  public String getName() {
    return name;
  }

  public AlertComponentApi setName(final String name) {
    this.name = name;
    return this;
  }

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

  public AlertApi getAlert() {
    return alert;
  }

  public AlertComponentApi setAlert(final AlertApi alert) {
    this.alert = alert;
    return this;
  }
}
