package org.apache.pinot.thirdeye.cube.entry;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import java.util.List;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;

public class SummaryUtils {

  /**
   * Checks the arguments of cube algorithm.
   *
   * @param dataset the name of dataset; cannot be null or empty.
   * @param metrics the list of metrics; it needs to contain at least one metric and they cannot
   *     be null or empty.
   * @param dimensions the dimensions to be explored; it needs to contains at least one
   *     dimensions.
   * @param dataFilters the filter to be applied on the Pinot query; cannot be null.
   * @param summarySize the summary size; needs to > 0.
   * @param depth the max depth of dimensions to be drilled down; needs to be >= 0.
   * @param hierarchies the hierarchy among dimensions; cannot be null.
   */
  public static void checkArguments(
      final String dataset,
      final List<String> metrics,
      final Dimensions dimensions,
      final Multimap<String, String> dataFilters,
      final int summarySize,
      final int depth,
      final List<List<String>> hierarchies) {
    checkArgument(!Strings.isNullOrEmpty(dataset));
    checkArgument(!metrics.isEmpty());
    for (String metric : metrics) {
      checkArgument(!Strings.isNullOrEmpty(metric));
    }
    Preconditions.checkNotNull(dimensions);
    checkArgument(dimensions.size() > 0);
    Preconditions.checkNotNull(dataFilters);
    checkArgument(summarySize > 1);
    Preconditions.checkNotNull(hierarchies);
    checkArgument(depth >= 0);
  }
}
