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
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class AdditiveCubeNodeTest {

  // Since CubeNode has cyclic reference between current node and parent node, the toString() will encounter
  // overflowStack exception if it doesn't take care of the cyclic reference carefully.
  @Test
  public void testToString() {
    AdditiveRow root = new AdditiveRow(new Dimensions(), new DimensionValues());
    AdditiveCubeNode rootNode = new AdditiveCubeNode(root);

    AdditiveRow child = new AdditiveRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("US")), 20, 30);
    AdditiveCubeNode childNode = new AdditiveCubeNode(1, 0, child, rootNode);

    childNode.toString();
  }

  @Test
  public void testEqualsAndHashCode() {
    AdditiveRow root1 = new AdditiveRow(new Dimensions(), new DimensionValues());
    AdditiveCubeNode rootNode1 = new AdditiveCubeNode(root1);

    AdditiveRow root2 = new AdditiveRow(new Dimensions(), new DimensionValues());
    AdditiveCubeNode rootNode2 = new AdditiveCubeNode(root2);

    assertThat(rootNode1).isEqualTo(rootNode2);
    Assertions.assertThat(CubeNodeUtils.equalHierarchy(rootNode1, rootNode2)).isTrue();
    assertThat(rootNode1).hasSameHashCodeAs(rootNode2);

    AdditiveRow root3 = new AdditiveRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("US")));
    CubeNode rootNode3 = new AdditiveCubeNode(root3);
    assertThat(rootNode1).isNotEqualTo(rootNode3);
    assertThat(rootNode1.hashCode()).isNotEqualTo(rootNode3.hashCode());
  }
}
