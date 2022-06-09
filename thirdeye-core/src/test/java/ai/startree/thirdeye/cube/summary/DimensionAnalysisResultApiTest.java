/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.summary;

import static ai.startree.thirdeye.cube.summary.NameTag.ALL_OTHERS;
import static ai.startree.thirdeye.cube.summary.Summary.roundUp;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.cube.additive.AdditiveCubeNode;
import ai.startree.thirdeye.cube.additive.AdditiveRow;
import ai.startree.thirdeye.cube.cost.BalancedCostFunction;
import ai.startree.thirdeye.cube.data.DimensionValues;
import ai.startree.thirdeye.cube.data.Dimensions;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.api.cube.SummaryResponseRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.data.Offset;
import org.testng.annotations.Test;

public class DimensionAnalysisResultApiTest {

  private static final Offset<Double> EPSILON = Offset.offset(0.0001);

  @Test
  public void testBuildDiffSummary() {
    // Create test case
    List<AdditiveCubeNode> cubeNodes = buildHierarchicalNodes();
    int rootIdx = cubeNodes.size() - 1;
    double baselineTotal = cubeNodes.get(rootIdx).getOriginalBaselineSize();
    double currentTotal = cubeNodes.get(rootIdx).getOriginalCurrentSize();
    double baselineSize = cubeNodes.get(rootIdx).getOriginalBaselineSize();
    double currentSize = cubeNodes.get(rootIdx).getOriginalCurrentSize();
    // Build the response
    DimensionAnalysisResultApi response = new DimensionAnalysisResultApi()
        .setBaselineTotal(baselineTotal)
        .setCurrentTotal(currentTotal)
        .setBaselineTotalSize(baselineSize)
        .setCurrentTotalSize(currentSize)
        .setGlobalRatio(roundUp(currentTotal / baselineTotal));
    Summary.buildDiffSummary(response, cubeNodes, new BalancedCostFunction());
    response.setMetric(new MetricApi().setName("testMetric"));

    // Validation
    List<SummaryResponseRow> responseRows = response.getResponseRows();
    assertThat(responseRows.size()).isEqualTo(2); // Our test summary contains only root (OTHER) and US node.
    List<SummaryResponseRow> expectedResponseRows = buildExpectedResponseRows();
    for (int i = 0; i < expectedResponseRows.size(); ++i) {
      SummaryResponseRow actualRow = responseRows.get(i);
      SummaryResponseRow expectedRow = expectedResponseRows.get(i);
      assertThat(actualRow.getNames()).isEqualTo(expectedRow.getNames());
      assertThat(actualRow.getOtherDimensionValues()).hasSameElementsAs(expectedRow.getOtherDimensionValues());
      assertThat(actualRow.getMoreOtherDimensionNumber()).isEqualTo(expectedRow.getMoreOtherDimensionNumber());
      assertThat(actualRow.getCost()).isCloseTo(expectedRow.getCost(), EPSILON);
      assertThat(actualRow.getBaselineValue()).isEqualTo(expectedRow.getBaselineValue());
      assertThat(actualRow.getCurrentValue()).isEqualTo(expectedRow.getCurrentValue());
      assertThat(actualRow.getChangePercentage()).isEqualTo(expectedRow.getChangePercentage());
      assertThat(actualRow.getSizeFactor()).isCloseTo(expectedRow.getSizeFactor(), EPSILON);
    }
  }

  /**
   * Provides data for this hierarchy:
   *       root
   *     /  |  \
   *    US  IN  FR
   */
  private List<List<AdditiveRow>> buildHierarchicalRows() {
    List<List<AdditiveRow>> hierarchicalRows = new ArrayList<>();
    List<String> dimensions = Collections.singletonList("country");

    // Root level
    List<AdditiveRow> rootLevel = new ArrayList<>();
    rootLevel.add(new AdditiveRow(new Dimensions(dimensions), new DimensionValues(), 45, 58));
    hierarchicalRows.add(rootLevel);

    // Level 1
    List<AdditiveRow> level1 = new ArrayList<>();
    AdditiveRow row1 =
        new AdditiveRow(new Dimensions(dimensions),
            new DimensionValues(Collections.singletonList("US")), 20, 30);
    level1.add(row1);

    AdditiveRow row2 =
        new AdditiveRow(new Dimensions(dimensions),
            new DimensionValues(Collections.singletonList("IN")), 10, 11);
    level1.add(row2);

    AdditiveRow row3 =
        new AdditiveRow(new Dimensions(dimensions),
            new DimensionValues(Collections.singletonList("FR")), 15, 17);
    level1.add(row3);

    hierarchicalRows.add(level1);

    return hierarchicalRows;
  }

  /**
   * Builds hierarchy:
   *      root (IN, FR)
   *     /
   *    US
   */
  private List<AdditiveCubeNode> buildHierarchicalNodes() {
    List<List<AdditiveRow>> rows = buildHierarchicalRows();
    // Root level
    AdditiveRow rootRow = rows.get(0).get(0);
    AdditiveCubeNode rootNode = new AdditiveCubeNode(rootRow);

    // Level 1
    AdditiveRow USRow = rows.get(1).get(0);
    AdditiveCubeNode USNode = new AdditiveCubeNode(1, 0, USRow, rootNode);

    AdditiveRow INRow = rows.get(1).get(1);
    AdditiveCubeNode INNode = new AdditiveCubeNode(1, 1, INRow, rootNode);

    AdditiveRow FRRow = rows.get(1).get(2);
    AdditiveCubeNode FRNode = new AdditiveCubeNode(1, 2, FRRow, rootNode);

    // Assume that US is the only child that is picked by the summary
    rootNode.removeNodeValues(USNode);

    List<AdditiveCubeNode> res = new ArrayList<>();
    res.add(USNode);
    // Root node is located at the end of this list.
    res.add(rootNode);

    return res;
  }

  /**
   * Builds expected hierarchy:
   *      root (IN, FR)
   *     /
   *    US
   */
  private List<SummaryResponseRow> buildExpectedResponseRows() {
    SummaryResponseRow root = new SummaryResponseRow();
    root.setNames(Collections.singletonList(ALL_OTHERS));
    root.setOtherDimensionValues(List.of("IN", "FR"));
    root.setCost(0d); // root doesn't have cost
    root.setBaselineValue(25d);
    root.setCurrentValue(28d);
    root.setSizeFactor(0.5145d);
    root.setChangePercentage((28d - 25d) / 25d * 100);

    SummaryResponseRow US = new SummaryResponseRow();
    US.setNames(Collections.singletonList("US"));
    US.setOtherDimensionValues(List.of());
    US.setCost(1.1587d);
    US.setBaselineValue(20d);
    US.setCurrentValue(30d);
    US.setSizeFactor(0.4854d); // UPDATE THIS
    US.setChangePercentage((30d - 20d) / 20d * 100);

    List<SummaryResponseRow> rows = new ArrayList<>();
    rows.add(root);
    rows.add(US);
    return rows;
  }
}
