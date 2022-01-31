package org.apache.pinot.thirdeye.spi.api;

import java.util.List;

public class EvaluationContextApi {

  /**
   * Dimension filters. Used when performing RCA.
   * Format is 'dim1=val1'.
   */
  private List<String> filters;

  public List<String> getFilters() {
    return filters;
  }

  public EvaluationContextApi setFilters(final List<String> filters) {
    this.filters = filters;
    return this;
  }
}
