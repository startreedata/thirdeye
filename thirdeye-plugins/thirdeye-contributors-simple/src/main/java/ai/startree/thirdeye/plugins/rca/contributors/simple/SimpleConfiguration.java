package ai.startree.thirdeye.plugins.rca.contributors.simple;

import static ai.startree.thirdeye.plugins.rca.contributors.simple.Cost.BAlANCED_SIMPLE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleConfiguration {

  private Cost costFunction = BAlANCED_SIMPLE;

  public Cost getCostFunction() {
    return costFunction;
  }

  public SimpleConfiguration setCostFunction(
      final Cost costFunction) {
    this.costFunction = costFunction;
    return this;
  }
}
