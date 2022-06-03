/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.node;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.cube.additive.AdditiveCubeNode;
import ai.startree.thirdeye.cube.additive.AdditiveRow;
import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 * Tests the hierarchy among cube nodes. The main challenge is handling parent and children nodes.
 */
public class CubeNodeTest {

  @Test
  public void testHierarchicalEquals() {
    AdditiveCubeNode rootNode1 = buildHierarchicalNodes();
    AdditiveCubeNode rootNode2 = buildHierarchicalNodes();

    Assertions.assertThat(CubeNodeUtils.equalHierarchy(rootNode1, rootNode2)).isTrue();
  }

  /**
   * Hierarchy 1:
   *      A
   *     / \
   *    B  C
   *
   * Hierarchy 2:
   *      A
   *
   * Failed because structure difference.
   */
  @Test
  public void testHierarchicalEqualsFail1() {
    AdditiveCubeNode rootNode1 = buildHierarchicalNodes();

    AdditiveRow rootRow = new AdditiveRow(new Dimensions(), new DimensionValues(), 30, 45);
    AdditiveCubeNode rootNode2 = new AdditiveCubeNode(rootRow);

    assertThat(rootNode1).isEqualTo(rootNode2);
    assertThat(CubeNodeUtils.equalHierarchy(rootNode1, rootNode2)).isFalse();
  }

  /**
   * Hierarchy 1:
   *      A
   *     / \
   *    B  C
   *
   * Hierarchy 2:
   *      A'
   *
   * Failed because data difference.
   */
  @Test
  public void testHierarchicalEqualsFail2() {
    AdditiveCubeNode rootNode1 = buildHierarchicalNodes();

    AdditiveRow rootRow = new AdditiveRow(new Dimensions(), new DimensionValues(), 20, 15);
    AdditiveCubeNode rootNode2 = new AdditiveCubeNode(rootRow);

    assertThat(rootNode1).isNotEqualTo(rootNode2);
    assertThat(CubeNodeUtils.equalHierarchy(rootNode1, rootNode2)).isFalse();
  }

  /**
   * Hierarchy 1:
   *      A
   *     ^ ^
   *     / \
   *    v  v
   *    B  C
   *
   * Hierarchy 2:
   *      A
   *      ^
   *       \
   *       v
   *       C
   *
   * Failed because Hierarchy 2's A doesn't have a reference to B.
   */
  @Test
  public void testHierarchicalEqualsFail3() {
    AdditiveCubeNode rootNode1 = buildHierarchicalNodes();

    List<List<AdditiveRow>> rows = buildHierarchicalRows();
    // Root level
    AdditiveRow rootRow = (AdditiveRow) rows.get(0).get(0);
    AdditiveCubeNode rootNode2 = new AdditiveCubeNode(rootRow);

    // Level 1
    AdditiveRow INRow = (AdditiveRow) rows.get(1).get(1);
    new AdditiveCubeNode(1, 1, INRow, rootNode2);

    assertThat(rootNode1).isEqualTo(rootNode2);
    assertThat(CubeNodeUtils.equalHierarchy(rootNode1, rootNode2)).isFalse();
  }

  /**
   * Provides data for this hierarchy:
   *      A
   *     / \
   *    B  C
   */
  private List<List<AdditiveRow>> buildHierarchicalRows() {
    List<List<AdditiveRow>> hierarchicalRows = new ArrayList<>();
    // Root level
    List<AdditiveRow> rootLevel = new ArrayList<>();
    rootLevel.add(new AdditiveRow(new Dimensions(), new DimensionValues(), 30, 45));
    hierarchicalRows.add(rootLevel);

    // Level 1
    List<AdditiveRow> level1 = new ArrayList<>();
    AdditiveRow row1 = new AdditiveRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("US")), 20, 30);
    level1.add(row1);

    AdditiveRow row2 = new AdditiveRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("IN")), 10, 15);
    level1.add(row2);

    hierarchicalRows.add(level1);

    return hierarchicalRows;
  }

  /**
   * Builds hierarchy:
   *   A
   *  / \
   * B   C
   */
  private AdditiveCubeNode buildHierarchicalNodes() {
    List<List<AdditiveRow>> rows = buildHierarchicalRows();
    // Root level
    AdditiveRow rootRow = (AdditiveRow) rows.get(0).get(0);
    AdditiveCubeNode rootNode = new AdditiveCubeNode(rootRow);

    // Level 1
    AdditiveRow USRow = (AdditiveRow) rows.get(1).get(0);
    new AdditiveCubeNode(1, 0, USRow, rootNode);

    AdditiveRow INRow = (AdditiveRow) rows.get(1).get(1);
    new AdditiveCubeNode(1, 1, INRow, rootNode);

    return rootNode;
  }
}
