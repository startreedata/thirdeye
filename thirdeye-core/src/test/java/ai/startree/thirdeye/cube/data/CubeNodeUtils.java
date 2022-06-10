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
package ai.startree.thirdeye.cube.data;

import java.util.List;
import org.apache.commons.lang3.ObjectUtils;

public class CubeNodeUtils {

  /**
   * Check if the hierarchical tree of the given two root nodes are the same.
   *
   * @param node1 the root node of the first hierarchical tree.
   * @param node2 the root node of the second hierarchical tree.
   * @return true if both hierarchical tree are the same.
   */
  public static boolean equalHierarchy(AdditiveCubeNode node1, AdditiveCubeNode node2) {
    return equalHierarchy(node1, null, node2, null);
  }

  public static boolean equalHierarchy(AdditiveCubeNode node1, AdditiveCubeNode node1Parent, AdditiveCubeNode node2,
      AdditiveCubeNode node2Parent) {
    if (!ObjectUtils.equals(node1, node2)) { // Return false if data of the nodes are different.
      return false;
    } else { // Check hierarchy if the two given nodes have the same data value.
      // Check parent reference
      if (node1Parent != null && node1.getParent() != node1Parent) {
        return false;
      }
      if (node2Parent != null && node2.getParent() != node2Parent) {
        return false;
      }

      // Check children reference
      if (node1.childrenSize() != node2.childrenSize()) {
        return false;
      }
      List<AdditiveCubeNode> children1 = node1.getChildren();
      List<AdditiveCubeNode> children2 = node2.getChildren();
      int size = children1.size();
      for (int i = 0; i < size; i++) {
        AdditiveCubeNode child1 = children1.get(i);
        AdditiveCubeNode child2 = children2.get(i);
        boolean sameChild = equalHierarchy(child1, node1, child2, node2);
        if (!sameChild) {
          return false;
        }
      }
      return true;
    }
  }
}
