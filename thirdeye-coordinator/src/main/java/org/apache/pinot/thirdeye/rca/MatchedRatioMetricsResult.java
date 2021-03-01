package org.apache.pinot.thirdeye.rca;

/**
 * The class to store the parsed numerator and denominator id.
 */
class MatchedRatioMetricsResult {

  boolean hasFound; // false is parsing is failed.
  long numeratorId; // numerator id if parsing is succeeded.
  long denominatorId; // denominator id if parsing is succeeded.

  /**
   * Construct the object that stores the parsed numerator and denominator id.
   *
   * @param hasFound false is parsing is failed.
   * @param numeratorId numerator id if parsing is succeeded.
   * @param denominatorId denominator id if parsing is succeeded.
   */
  MatchedRatioMetricsResult(boolean hasFound, long numeratorId, long denominatorId) {
    this.hasFound = hasFound;
    this.numeratorId = numeratorId;
    this.denominatorId = denominatorId;
  }
}
