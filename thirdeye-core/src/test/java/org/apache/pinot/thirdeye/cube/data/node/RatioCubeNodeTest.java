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

package org.apache.pinot.thirdeye.cube.data.node;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import org.apache.pinot.thirdeye.cube.data.dbrow.DimensionValues;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.ratio.RatioCubeNode;
import org.apache.pinot.thirdeye.cube.ratio.RatioRow;
import org.testng.annotations.Test;

public class RatioCubeNodeTest {

  @Test
  public void testSide() {
    RatioRow root = new RatioRow(new Dimensions(), new DimensionValues());
    root.setBaselineNumeratorValue(100);
    root.setBaselineDenominatorValue(200);
    root.setCurrentNumeratorValue(150);
    root.setCurrentDenominatorValue(250);
    RatioCubeNode rootNode = new RatioCubeNode(root);

    // Ratio node with clear side()
    RatioRow rowUS = new RatioRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("US")));
    rowUS.setBaselineNumeratorValue(50); // 50 left
    rowUS.setBaselineDenominatorValue(120); // 80 left
    rowUS.setCurrentNumeratorValue(80); // 70 left
    rowUS.setCurrentDenominatorValue(180); // 70 left
    RatioCubeNode nodeUS = new RatioCubeNode(1, 0, rowUS, rootNode);
    assertThat(nodeUS.changeRatio()).isEqualTo((80 / 180d) / (50d / 120d));
    assertThat(nodeUS.side()).isEqualTo(nodeUS.changeRatio() > 1d);

    // Ratio node doesn't have baseline
    RatioRow rowIN = new RatioRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("IN")));
    rowIN.setBaselineNumeratorValue(0); // 50 left
    rowIN.setBaselineDenominatorValue(0); // 80 left
    rowIN.setCurrentNumeratorValue(70); // 0 left
    rowIN.setCurrentDenominatorValue(50); // 20 left
    RatioCubeNode nodeIN = new RatioCubeNode(1, 1, rowIN, rootNode);
    assertThat(nodeIN.changeRatio()).isEqualTo((Double) Double.NaN); // The ratio will be inferred by algorithm itself
    assertThat(nodeIN.side()).isEqualTo(nodeIN.getCurrentValue() > rootNode.getCurrentValue());

    // Ratio node doesn't have baseline
    RatioRow rowFR = new RatioRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("IN")));
    rowFR.setBaselineNumeratorValue(25); // 25 left
    rowFR.setBaselineDenominatorValue(60); // 20 left
    rowFR.setCurrentNumeratorValue(0); // 0 left
    rowFR.setCurrentDenominatorValue(0); // 20 left
    RatioCubeNode nodeFR = new RatioCubeNode(1, 2, rowFR, rootNode);
    assertThat(nodeFR.changeRatio()).isEqualTo((Double) Double.NaN); // The ratio will be inferred by algorithm itself
    // The side of FR is UP because it's baseline has lower ratio than it's parent; hence, we expect that removing FR
    // will move the metric upward.
    assertThat(nodeFR.side()).isEqualTo(nodeFR.getBaselineValue() < rootNode.getBaselineValue());
  }

  // Since CubeNode has cyclic reference between current node and parent node, the toString() will encounter
  // overflowStack exception if it doesn't take care of the cyclic reference carefully.
  @Test
  public void testToString() {
    RatioRow root = new RatioRow(new Dimensions(), new DimensionValues());
    RatioCubeNode rootNode = new RatioCubeNode(root);

    RatioRow child = new RatioRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("US")));
    child.setBaselineNumeratorValue(20);
    child.setBaselineDenominatorValue(20);
    child.setCurrentNumeratorValue(30);
    child.setCurrentDenominatorValue(31);
    RatioCubeNode childNode = new RatioCubeNode(1, 0, child, rootNode);

    System.out.println(childNode.toString());
  }

  @Test
  public void testEqualsAndHashCode() {
    RatioRow root1 = new RatioRow(new Dimensions(), new DimensionValues());
    CubeNode rootNode1 = new RatioCubeNode(root1);

    RatioRow root2 = new RatioRow(new Dimensions(), new DimensionValues());
    CubeNode rootNode2 = new RatioCubeNode(root2);

    assertThat(rootNode1).isEqualTo(rootNode2);
    assertThat(CubeNodeUtils.equalHierarchy(rootNode1, rootNode2)).isTrue();
    assertThat(rootNode1.hashCode()).isEqualTo(rootNode2.hashCode());

    RatioRow root3 = new RatioRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("US")));
    CubeNode rootNode3 = new RatioCubeNode(root3);
    assertThat(rootNode1).isNotEqualTo(rootNode3);
    assertThat(rootNode1.hashCode()).isNotEqualTo(rootNode3.hashCode());
  }
}
