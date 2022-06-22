/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ai.startree.thirdeye.plugins.rca.contributors.cube.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
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
    assertThat(CubeNodeUtils.equalHierarchy(rootNode1, rootNode2)).isTrue();
    assertThat(rootNode1).hasSameHashCodeAs(rootNode2);

    AdditiveRow root3 = new AdditiveRow(new Dimensions(Collections.singletonList("country")),
        new DimensionValues(Collections.singletonList("US")));
    AdditiveCubeNode rootNode3 = new AdditiveCubeNode(root3);
    assertThat(rootNode1).isNotEqualTo(rootNode3);
    assertThat(rootNode1.hashCode()).isNotEqualTo(rootNode3.hashCode());
  }
}
