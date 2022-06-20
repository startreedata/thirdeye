package ai.startree.thirdeye.plugins.rca.contributors.shallow;

import static ai.startree.thirdeye.plugins.rca.contributors.shallow.Cost.BAlANCED_SIMPLE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShallowConfiguration {

  private Cost costFunction = BAlANCED_SIMPLE;

  public Cost getCostFunction() {
    return costFunction;
  }

  public ShallowConfiguration setCostFunction(
      final Cost costFunction) {
    this.costFunction = costFunction;
    return this;
  }
}
