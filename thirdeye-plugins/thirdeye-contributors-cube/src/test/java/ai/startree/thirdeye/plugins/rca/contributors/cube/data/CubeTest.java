/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.rca.contributors.cube.data;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.api.cube.DimensionCost;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.Test;

public class CubeTest {

  private static final String DIM_COUNTRY = "country";
  private static final String DIM_PAGE = "page";
  private static final String DIM_CONTINENT = "continent";

  @Test
  public void testSortDimensionNoHierarchy() {
    List<DimensionCost> dimensionCosts = new ArrayList<>();
    dimensionCosts.add(new DimensionCost(DIM_COUNTRY, 10d));
    dimensionCosts.add(new DimensionCost(DIM_PAGE, 8d));
    dimensionCosts.add(new DimensionCost(DIM_CONTINENT, 5d));

    Dimensions expectedSortedDimensions1 = new Dimensions(List.of(DIM_COUNTRY,
        DIM_PAGE,
        DIM_CONTINENT));
    // No hierarchy
    Dimensions sortedDimensionsMaxDepth = Cube.sortDimensions(dimensionCosts,
        3,
        Collections.emptyList());
    assertThat(sortedDimensionsMaxDepth).isEqualTo(expectedSortedDimensions1);

    Dimensions sortedDimensionsExceedMaxDepth = Cube.sortDimensions(dimensionCosts,
        4,
        Collections.emptyList());
    assertThat(sortedDimensionsExceedMaxDepth).isEqualTo(expectedSortedDimensions1);
  }

  @Test
  public void testSortDimensionWithHierarchy() {
    final List<DimensionCost> dimensionCosts = new ArrayList<>();
    dimensionCosts.add(new DimensionCost(DIM_COUNTRY, 10d));
    dimensionCosts.add(new DimensionCost(DIM_PAGE, 8d));
    dimensionCosts.add(new DimensionCost(DIM_CONTINENT, 5d));

    final List<DimensionCost> dimensionCostsUnmodified = List.copyOf(dimensionCosts);

    // Hierarchy with depth = 1
    Dimensions sortedDimensionsDepth1 = Cube.sortDimensions(dimensionCosts,
        1,
        Collections.singletonList(List.of(DIM_CONTINENT, DIM_COUNTRY)));
    Dimensions expectedSortedDimensions1 = new Dimensions(List.of(DIM_COUNTRY));
    assertThat(sortedDimensionsDepth1).isEqualTo(expectedSortedDimensions1);

    // Hierarchy with depth = 2
    Dimensions sortedDimensionsDepth2 = Cube.sortDimensions(dimensionCosts,
        2,
        Collections.singletonList(List.of(DIM_CONTINENT, DIM_COUNTRY)));
    Dimensions expectedSortedDimensions2 = new Dimensions(List.of(DIM_COUNTRY, DIM_PAGE));
    assertThat(sortedDimensionsDepth2).isEqualTo(expectedSortedDimensions2);

    // Hierarchy with depth = 3
    Dimensions sortedDimensionsDepth3 = Cube.sortDimensions(dimensionCosts,
        3,
        Collections.singletonList(List.of(DIM_CONTINENT, DIM_COUNTRY)));
    Dimensions expectedSortedDimensions3 = new Dimensions(List.of(DIM_PAGE,
        DIM_CONTINENT,
        DIM_COUNTRY));
    assertThat(sortedDimensionsDepth3).isEqualTo(expectedSortedDimensions3);

    // test that there are no side effect on the dimensions list
    assertThat(dimensionCosts).isEqualTo(dimensionCostsUnmodified);
  }

  @Test
  public void testCalculateSortedDimensionCost() {
    List<DimNameValueCostEntry> costSet = new ArrayList<>();
    costSet.add(new DimNameValueCostEntry(DIM_COUNTRY, "US", 0, 0, 0d, 0d, 0, 0, 0, 7));
    costSet.add(new DimNameValueCostEntry(DIM_COUNTRY, "IN", 0, 0, 0d, 0d, 0, 0, 0, 3));
    costSet.add(new DimNameValueCostEntry(DIM_CONTINENT, "N. America", 0, 0, 0d, 0d, 0, 0, 0, 4));
    costSet.add(new DimNameValueCostEntry(DIM_CONTINENT, "S. America", 0, 0, 0d, 0d, 0, 0, 0, 1));
    costSet.add(new DimNameValueCostEntry(DIM_PAGE, "front_page", 0, 0, 0d, 0d, 0, 0, 0, 4));
    costSet.add(new DimNameValueCostEntry(DIM_PAGE, DIM_PAGE, 0, 0, 0d, 0d, 0, 0, 0, 3));
    costSet.add(new DimNameValueCostEntry(DIM_PAGE, "page2", 0, 0, 0d, 0d, 0, 0, 0, 1));

    List<DimensionCost> actualDimensionCosts = Cube.calculateSortedDimensionCost(costSet);

    List<DimensionCost> expectedDimensionCosts = new ArrayList<>();
    expectedDimensionCosts.add(new DimensionCost(DIM_COUNTRY, 10d));
    expectedDimensionCosts.add(new DimensionCost(DIM_PAGE, 8d));
    expectedDimensionCosts.add(new DimensionCost(DIM_CONTINENT, 5d));

    assertThat(actualDimensionCosts).isEqualTo(expectedDimensionCosts);
  }

  @Test
  public void testHierarchyRowToHierarchyNode() {
    List<List<AdditiveRow>> hierarchicalRows = buildHierarchicalRows();
    List<List<AdditiveCubeNode>> actualNodes = Cube.dataRowToCubeNode(hierarchicalRows,
        new Dimensions(List.of(DIM_COUNTRY, DIM_PAGE)));

    List<List<AdditiveCubeNode>> expectedNodes = expectedHierarchicalNodes();

    // Test if the data is current; the reference (i.e., tree structure is not tested)
    assertThat(actualNodes).isEqualTo(expectedNodes);

    // Test the structure of the hierarchy
    assertThat(CubeNodeUtils.equalHierarchy(actualNodes.get(0).get(0),
        expectedNodes.get(0).get(0))).isTrue();
  }

  private List<List<AdditiveRow>> buildHierarchicalRows() {
    List<List<AdditiveRow>> hierarchicalRows = new ArrayList<>();
    // Root level
    {
      List<AdditiveRow> rootLevel = new ArrayList<>();
      rootLevel.add(new AdditiveRow(new Dimensions(), new DimensionValues(), 30, 45));
      hierarchicalRows.add(rootLevel);
    }
    // Level 1
    List<AdditiveRow> level1 = new ArrayList<>();
    level1.add(
        new AdditiveRow(
            new Dimensions(Collections.singletonList(DIM_COUNTRY)),
            new DimensionValues(Collections.singletonList("US")),
            20,
            30));
    level1.add(
        new AdditiveRow(new Dimensions(Collections.singletonList(DIM_COUNTRY)),
            new DimensionValues(Collections.singletonList("IN")),
            10,
            15));
    hierarchicalRows.add(level1);
    // Level 2
    List<AdditiveRow> level2 = new ArrayList<>();
    level2.add(new AdditiveRow(new Dimensions(List.of(DIM_COUNTRY, DIM_PAGE)),
        new DimensionValues(List.of("US", "page1")),
        8,
        10));
    level2.add(new AdditiveRow(new Dimensions(List.of(DIM_COUNTRY, DIM_PAGE)),
        new DimensionValues(List.of("US", "page2")),
        12,
        20));
    level2.add(new AdditiveRow(new Dimensions(List.of(DIM_COUNTRY, DIM_PAGE)),
        new DimensionValues(List.of("IN", "page1")),
        10,
        15));
    hierarchicalRows.add(level2);

    return hierarchicalRows;
  }

  private List<List<AdditiveCubeNode>> expectedHierarchicalNodes() {
    List<List<AdditiveRow>> rows = buildHierarchicalRows();
    List<List<AdditiveCubeNode>> hierarchicalNodes = new ArrayList<>();
    // Root level
    List<AdditiveCubeNode> rootLevel = new ArrayList<>();
    hierarchicalNodes.add(rootLevel);

    AdditiveRow rootRow = rows.get(0).get(0);
    AdditiveCubeNode rootNode = new AdditiveCubeNode((AdditiveRow) rootRow);
    rootLevel.add(rootNode);

    // Level 1
    List<AdditiveCubeNode> level1 = new ArrayList<>();
    hierarchicalNodes.add(level1);

    AdditiveRow USRow = (AdditiveRow) rows.get(1).get(0);
    AdditiveCubeNode USNode = new AdditiveCubeNode(1, 0, USRow, rootNode);
    level1.add(USNode);

    AdditiveRow INRow = (AdditiveRow) rows.get(1).get(1);
    AdditiveCubeNode INNode = new AdditiveCubeNode(1, 1, INRow, rootNode);
    level1.add(INNode);

    // Level 2
    List<AdditiveCubeNode> level2 = new ArrayList<>();
    hierarchicalNodes.add(level2);

    AdditiveRow USPage1Row = (AdditiveRow) rows.get(2).get(0);
    AdditiveCubeNode USPage1Node = new AdditiveCubeNode(2, 0, USPage1Row, USNode);
    level2.add(USPage1Node);

    AdditiveRow USPage2Row = (AdditiveRow) rows.get(2).get(1);
    AdditiveCubeNode USPage2Node = new AdditiveCubeNode(2, 1, USPage2Row, USNode);
    level2.add(USPage2Node);

    AdditiveRow INPage1Row = (AdditiveRow) rows.get(2).get(2);
    AdditiveCubeNode INPage1Node = new AdditiveCubeNode(2, 2, INPage1Row, INNode);
    level2.add(INPage1Node);

    return hierarchicalNodes;
  }
}
