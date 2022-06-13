/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import com.google.common.math.DoubleMath;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CubeUtils {

  private static final double epsilon = 0.0001;

  /**
   * Removes dimensions from the given list of dimensions, which have single value constraint in the
   * predicate list. Eg remove browser when there is a predicate browser=chrome
   * This is because setting a filter with such one value predicate implies that the final data cube
   * does not contain other dimension values. Thus, the
   * summary algorithm could simply ignore that dimension (because the cube does not have any other
   * values to compare with in that dimension).
   *
   * @param dimensions the list of dimensions to be modified.
   * @param filterSets the filter to be applied on the data cube.
   * @return the list of dimensions that should be used for retrieving the data for summary algorithm.
   */
  public static Dimensions shrinkDimensionsByFilterSets(Dimensions dimensions,
      List<Predicate> filterSets) {
    Set<String> dimensionsToRemove = filterSets.stream().filter(p -> p.getOper() != OPER.IN)
        .map(Predicate::getLhs)
        .collect(Collectors.toUnmodifiableSet());
    List<String> dimensionsToRetain = new ArrayList<>(dimensions.names());
    dimensionsToRetain.removeAll(dimensionsToRemove);
    return new Dimensions(dimensionsToRetain);
  }

  /**
   * Return the results of a minus b. If the result is very close to zero, then zero is returned.
   * This method is use to prevent the precision issue of double from inducing -0.00000000000000001,
   * which is
   * actually zero.
   *
   * @param a a double value.
   * @param b the other double value.
   * @return the results of a minus b.
   */
  public static double doubleMinus(double a, double b) {
    double ret = a - b;
    if (DoubleMath.fuzzyEquals(ret, 0, epsilon)) {
      return 0.0;
    } else {
      return ret;
    }
  }

  /**
   * Flips parent's change ratio if the change ratios of current node and its parent are different.
   *
   * @param baselineValue the baseline value of a node.
   * @param currentValue the current value of a node.
   * @param ratio the (parent) ratio to be flipped.
   * @return the ratio that has the same direction as the change direction of baseline and current
   *     value.
   */
  public static double ensureChangeRatioDirection(double baselineValue, double currentValue,
      double ratio) {
    // case: value goes down but parent's value goes up
    if (DoubleMath.fuzzyCompare(baselineValue, currentValue, epsilon) > 0
        && DoubleMath.fuzzyCompare(ratio, 1, epsilon) > 0) {
      if (Double.compare(ratio, 2) >= 0) {
        ratio = 2d - (ratio - ((long) ratio - 1));
      } else {
        ratio = 2d - ratio;
      }
      // case: value goes up but parent's value goes down
    } else if (DoubleMath.fuzzyCompare(baselineValue, currentValue, epsilon) < 0
        && DoubleMath.fuzzyCompare(ratio, 1, epsilon) < 0) {
      ratio = 2d - ratio;
    }
    // return the original ratio for other cases.
    return ratio;
  }
}
