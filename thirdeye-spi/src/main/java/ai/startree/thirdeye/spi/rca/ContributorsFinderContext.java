package ai.startree.thirdeye.spi.rca;

import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import java.util.Map;

public class ContributorsFinderContext {

  private AggregationLoader aggregationLoader;

  private Map<String, Object> params;

  public Map<String, Object> getParams() {
    return params;
  }

  public ContributorsFinderContext setParams(
      final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public AggregationLoader getAggregationLoader() {
    return aggregationLoader;
  }

  public ContributorsFinderContext setAggregationLoader(
      final AggregationLoader aggregationLoader) {
    this.aggregationLoader = aggregationLoader;
    return this;
  }
}
