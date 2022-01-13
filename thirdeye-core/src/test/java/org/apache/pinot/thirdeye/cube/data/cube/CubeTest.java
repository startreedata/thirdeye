/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.cube.data.cube;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.pinot.thirdeye.cube.additive.AdditiveCubeNode;
import org.apache.pinot.thirdeye.cube.additive.AdditiveRow;
import org.apache.pinot.thirdeye.cube.data.dbrow.DimensionValues;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.data.dbrow.Row;
import org.apache.pinot.thirdeye.cube.data.node.CubeNode;
import org.apache.pinot.thirdeye.cube.data.node.CubeNodeUtils;
import org.apache.pinot.thirdeye.spi.api.cube.DimensionCost;
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
    List<List<Row>> hierarchicalRows = buildHierarchicalRows();
    List<List<CubeNode>> actualNodes = Cube.dataRowToCubeNode(hierarchicalRows,
        new Dimensions(List.of(DIM_COUNTRY, DIM_PAGE)));

    List<List<CubeNode>> expectedNodes = expectedHierarchicalNodes();

    // Test if the data is current; the reference (i.e., tree structure is not tested)
    assertThat(actualNodes).isEqualTo(expectedNodes);

    // Test the structure of the hierarchy
    assertThat(CubeNodeUtils.equalHierarchy(actualNodes.get(0).get(0),
        expectedNodes.get(0).get(0))).isTrue();
  }

  private List<List<Row>> buildHierarchicalRows() {
    List<List<Row>> hierarchicalRows = new ArrayList<>();
    // Root level
    {
      List<Row> rootLevel = new ArrayList<>();
      rootLevel.add(new AdditiveRow(new Dimensions(), new DimensionValues(), 30, 45));
      hierarchicalRows.add(rootLevel);
    }
    // Level 1
    List<Row> level1 = new ArrayList<>();
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
    List<Row> level2 = new ArrayList<>();
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

  private List<List<CubeNode>> expectedHierarchicalNodes() {
    List<List<Row>> rows = buildHierarchicalRows();
    List<List<CubeNode>> hierarchicalNodes = new ArrayList<>();
    // Root level
    List<CubeNode> rootLevel = new ArrayList<>();
    hierarchicalNodes.add(rootLevel);

    Row rootRow = rows.get(0).get(0);
    AdditiveCubeNode rootNode = new AdditiveCubeNode((AdditiveRow) rootRow);
    rootLevel.add(rootNode);

    // Level 1
    List<CubeNode> level1 = new ArrayList<>();
    hierarchicalNodes.add(level1);

    AdditiveRow USRow = (AdditiveRow) rows.get(1).get(0);
    AdditiveCubeNode USNode = new AdditiveCubeNode(1, 0, USRow, rootNode);
    level1.add(USNode);

    AdditiveRow INRow = (AdditiveRow) rows.get(1).get(1);
    AdditiveCubeNode INNode = new AdditiveCubeNode(1, 1, INRow, rootNode);
    level1.add(INNode);

    // Level 2
    List<CubeNode> level2 = new ArrayList<>();
    hierarchicalNodes.add(level2);

    AdditiveRow USPage1Row = (AdditiveRow) rows.get(2).get(0);
    CubeNode USPage1Node = new AdditiveCubeNode(2, 0, USPage1Row, USNode);
    level2.add(USPage1Node);

    AdditiveRow USPage2Row = (AdditiveRow) rows.get(2).get(1);
    CubeNode USPage2Node = new AdditiveCubeNode(2, 1, USPage2Row, USNode);
    level2.add(USPage2Node);

    AdditiveRow INPage1Row = (AdditiveRow) rows.get(2).get(2);
    CubeNode INPage1Node = new AdditiveCubeNode(2, 2, INPage1Row, INNode);
    level2.add(INPage1Node);

    return hierarchicalNodes;
  }
}
