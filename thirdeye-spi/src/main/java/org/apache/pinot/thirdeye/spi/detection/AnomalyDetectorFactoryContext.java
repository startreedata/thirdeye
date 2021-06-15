package org.apache.pinot.thirdeye.spi.detection;

import java.util.Map;

public class AnomalyDetectorFactoryContext {

  private Map<String, Object> properties;
  private InputDataFetcher inputDataFetcher;

  public Map<String, Object> getProperties() {
    return properties;
  }

  public AnomalyDetectorFactoryContext setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public InputDataFetcher getInputDataFetcher() {
    return inputDataFetcher;
  }

  public AnomalyDetectorFactoryContext setInputDataFetcher(
      final InputDataFetcher inputDataFetcher) {
    this.inputDataFetcher = inputDataFetcher;
    return this;
  }
}
