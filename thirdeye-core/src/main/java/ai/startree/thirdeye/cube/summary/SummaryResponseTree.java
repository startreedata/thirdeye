/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.summary;

import ai.startree.thirdeye.cube.additive.AdditiveCubeNode;
import ai.startree.thirdeye.cube.cost.CostFunction;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryResponseTree {

  private static final Logger LOG = LoggerFactory.getLogger(SummaryResponseTree.class);

  @JsonProperty("dimensions")
  private final List<String> dimensions = new ArrayList<>();
  private final List<AdditiveCubeNode> hierarchicalNodes = new ArrayList<>();

  public static List<AdditiveCubeNode> sortResponseTree(List<AdditiveCubeNode> nodes, int levelCount,
      CostFunction costFunction) {
    SummaryResponseTree responseTree = new SummaryResponseTree();

    // Build the header
    Dimensions dimensions = nodes.get(0).getDimensions();
    responseTree.dimensions.addAll(dimensions.names().subList(0, levelCount));

    // take the first node as the top Node
    AdditiveCubeNode topNode = nodes.get(0);
    // replace topNode by the root node if it is found
    for (AdditiveCubeNode node : nodes) {
      if (node.getLevel() == 0) {
        topNode = node;
        break;
      }
    }

    List<SummaryResponseTreeNode> treeNodes = new ArrayList<>();
    // Build the response tree
    nodes.sort(Collections.reverseOrder(Summary.NODE_COMPARATOR)); // pre-order traversal
    for (AdditiveCubeNode node : nodes) {
      SummaryResponseTreeNode treeNode = new SummaryResponseTreeNode();
      treeNode.cubeNode = node;
      treeNode.level = node.getLevel();
      treeNodes.add(treeNode);
    }

    //    Connecting child and parent response tree node. Note: response tree is not a perfect tree like the tree
    //    of HierarchyNodes, because in response tree a node's direct parent may be missing.
    //    In that case, we have to bootstrap the search until a higher level parent, which also exists in the response
    //    tree, is found.
    //    Pre-condition: treeNodes are sorted in the pre-order fashion when projecting the nodes back to the tree of
    //                   CubeNode.
    SummaryResponseTreeNode preTreeNode = null;
    for (SummaryResponseTreeNode treeNode : treeNodes) {
      if (preTreeNode != null) {
        SummaryResponseTreeNode parent = preTreeNode.getCommonParent(treeNode.getDimensionValues());
        treeNode.parent = parent;
        parent.children.add(treeNode);
      }
      preTreeNode = treeNode;
    }

    // Sort the children of each node by their cost
    sortChildNodes(treeNodes.get(0), topNode, costFunction);

    // Put the nodes to a flattened array
    insertChildNodes(treeNodes.get(0), responseTree.hierarchicalNodes);

    return responseTree.hierarchicalNodes;
  }

  private static void insertChildNodes(SummaryResponseTreeNode node,
      List<AdditiveCubeNode> hierarchicalNodes) {
    if (node.cubeNode != null) {
      hierarchicalNodes.add(node.cubeNode);
    }
    for (SummaryResponseTreeNode child : node.children) {
      insertChildNodes(child, hierarchicalNodes);
    }
  }

  /**
   * A recursive function to sort response tree.
   */
  private static void sortChildNodes(SummaryResponseTreeNode node, final AdditiveCubeNode topNode,
      CostFunction costFunction) {
    if (node.children.size() == 0) {
      return;
    }
    for (SummaryResponseTreeNode child : node.children) {
      sortChildNodes(child, topNode, costFunction);
    }
    double ratio = node.currentChangeRatio();
    for (SummaryResponseTreeNode child : node.children) {
      computeCost(child, ratio, topNode, costFunction);
    }
    node.children.sort(Collections.reverseOrder(new SummaryResponseTreeNodeCostComparator()));
  }

  private static void computeCost(SummaryResponseTreeNode node, double targetChangeRatio,
      final AdditiveCubeNode topNode, CostFunction costFunction) {
    if (node.cubeNode != null) {
      double nodeCost = costFunction
          .computeCost(targetChangeRatio,
              node.getBaselineValue(),
              node.getCurrentValue(),
              node.getBaselineSize(),
              node.getCurrentSize(),
              topNode.getOriginalBaselineSize(),
              topNode.getOriginalCurrentSize(),
              topNode.getOriginalBaselineSize(),
              topNode.getOriginalCurrentSize());
      node.cubeNode.setCost(nodeCost);
      node.subTreeCost = nodeCost;
    }
    for (SummaryResponseTreeNode child : node.children) {
      computeCost(child, targetChangeRatio, topNode, costFunction);
      node.subTreeCost += child.subTreeCost;
    }
  }

  public static class SummaryResponseTreeNodeCostComparator implements
      Comparator<SummaryResponseTreeNode> {

    @Override
    public int compare(SummaryResponseTreeNode o1, SummaryResponseTreeNode o2) {
      return Double.compare(o1.subTreeCost, o2.subTreeCost);
    }
  }

  public static class SummaryResponseTreeNode {

    AdditiveCubeNode cubeNode; // If it is null, this node is a dummy node.
    double subTreeCost;
    int level;

    @JsonIgnore
    SummaryResponseTreeNode parent;

    List<SummaryResponseTreeNode> children = new ArrayList<>();

    public List<String> getDimensionValues() {
      return cubeNode.getDimensionValues().values();
    }

    public void setLevel(int level) {
      this.level = level;
    }

    public int getLevel() {
      return level;
    }

    public double getBaselineValue() {
      return cubeNode.getBaselineSize();
    }

    public double getCurrentValue() {
      return cubeNode.getCurrentSize();
    }

    public double getBaselineSize() {
      return cubeNode.getBaselineSize();
    }

    public double getCurrentSize() {
      return cubeNode.getCurrentSize();
    }

    public double currentChangeRatio() {
      if (cubeNode != null) {
        return cubeNode.changeRatio();
      } else {
        SummaryResponseTreeNode parent = this;
        do {
          if (parent.cubeNode == null) {
            parent = parent.parent;
          } else {
            break;
          }
        } while (true);
        return parent.currentChangeRatio();
      }
    }

    @JsonIgnore
    /**
     * Return the common parent node according to the given dimension values. There always exists one common
     * parent: the root of the tree.
     */
    public SummaryResponseTreeNode getCommonParent(List<String> otherDimensionValues) {
      // Calculate the level of the common parent
      int targetLevel = 0;
      Iterator<String> otherIte = otherDimensionValues.iterator();
      for (targetLevel = 0; targetLevel < getLevel(); ++targetLevel) {
        if (otherIte.hasNext()) {
          String otherDimensionValue = otherIte.next();
          if (!getDimensionValues().get(targetLevel).equals(otherDimensionValue)) {
            break;
          } // else continue
        } else {
          break;
        }
      }

      // Return the common parent
      SummaryResponseTreeNode node = this;
      SummaryResponseTreeNode preNode = this;
      while (true) {
        if (node.getLevel() == targetLevel) {
          return node;
        }
        if (node.getLevel() < targetLevel) {
          // current node becomes the missing common parent and move itself to a new node
          SummaryResponseTreeNode newChildNode = new SummaryResponseTreeNode();
          swapContent(preNode, newChildNode);
          newChildNode.parent = preNode;
          preNode.children.add(newChildNode);
          preNode.level = targetLevel;
          return preNode;
        } else {
          preNode = node;
          node = node.parent;
        }
      }
    }

    private void swapContent(SummaryResponseTreeNode A, SummaryResponseTreeNode B) {
      AdditiveCubeNode tmpNode = A.cubeNode;
      A.cubeNode = B.cubeNode;
      B.cubeNode = tmpNode;
      List<SummaryResponseTreeNode> tmpChildren = A.children;
      A.children = B.children;
      B.children = tmpChildren;
      int tmpLevel = A.level;
      A.level = B.level;
      B.level = tmpLevel;
    }
  }
}
