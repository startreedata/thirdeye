/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.cube.summary;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.cube.cost.BalancedCostFunction;
import org.apache.pinot.thirdeye.cube.cost.CostFunction;
import org.apache.pinot.thirdeye.cube.data.cube.Cube;
import org.apache.pinot.thirdeye.cube.data.cube.DimNameValueCostEntry;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.data.node.CubeNode;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.api.cube.DimensionCost;
import org.apache.pinot.thirdeye.spi.api.cube.SummaryGainerLoserResponseRow;
import org.apache.pinot.thirdeye.spi.api.cube.SummaryResponseRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Summary {

  public final static int MAX_GAINER_LOSER_COUNT = 5;
  public final static NumberFormat DOUBLE_FORMATTER = new DecimalFormat("#0.0000");
  public static final String INFINITE = "";
  public static final String ALL = "(ALL)";
  public static final String NOT_ALL = "(ALL)-";
  public static final String EMPTY = "";
  public static final String NOT_AVAILABLE = "-na-";

  static final NodeDimensionValuesComparator NODE_COMPARATOR = new NodeDimensionValuesComparator();
  private static final Logger LOG = LoggerFactory.getLogger(Summary.class);

  private final Cube cube;
  private final int maxLevelCount;
  private final double globalBaselineValue;
  private final double globalCurrentValue;
  private final double globalBaselineSize;
  private final double globalCurrentSize;
  private final CostFunction costFunction;
  private final RowInserter basicRowInserter;
  private final List<DimNameValueCostEntry> costSet;
  private final List<DimensionCost> sortedDimensionCosts;
  private int levelCount;
  private List<DPArray> dpArrays;
  private RowInserter oneSideErrorRowInserter;
  private RowInserter leafRowInserter;

  public Summary(Cube cube, CostFunction costFunction) {
    this.cube = cube;
    this.maxLevelCount = cube.getDimensions().size();
    this.globalBaselineValue = cube.getBaselineTotal();
    this.globalCurrentValue = cube.getCurrentTotal();
    this.globalBaselineSize = cube.getBaselineTotalSize();
    this.globalCurrentSize = cube.getCurrentTotalSize();
    this.levelCount = this.maxLevelCount;
    this.costSet = cube.getCostSet();
    this.sortedDimensionCosts = cube.getSortedDimensionCosts();

    this.costFunction = costFunction;
    this.basicRowInserter = new BasicRowInserter(costFunction);
    this.oneSideErrorRowInserter = basicRowInserter;
    this.leafRowInserter = basicRowInserter;
  }

  public static void buildGainerLoserGroup(
      final DimensionAnalysisResultApi dimensionAnalysisResultApi,
      List<DimNameValueCostEntry> costSet) {
    for (DimNameValueCostEntry dimNameValueCostEntry : costSet) {
      if (Double.compare(dimNameValueCostEntry.getCost(), 0d) <= 0) {
        continue;
      }
      if (dimNameValueCostEntry.getCurrentValue() >= dimNameValueCostEntry.getBaselineValue()
          && dimensionAnalysisResultApi.getGainer().size() < MAX_GAINER_LOSER_COUNT) {
        dimensionAnalysisResultApi.getGainer().add(
            buildGainerLoserRow(dimensionAnalysisResultApi, dimNameValueCostEntry));
      } else if (dimNameValueCostEntry.getCurrentValue() < dimNameValueCostEntry.getBaselineValue()
          && dimensionAnalysisResultApi.getLoser().size() < MAX_GAINER_LOSER_COUNT) {
        dimensionAnalysisResultApi.getLoser().add(
            buildGainerLoserRow(dimensionAnalysisResultApi, dimNameValueCostEntry));
      }
      if (dimensionAnalysisResultApi.getGainer().size() >= MAX_GAINER_LOSER_COUNT
          && dimensionAnalysisResultApi.getLoser().size() >= MAX_GAINER_LOSER_COUNT) {
        break;
      }
    }
  }

  public static SummaryGainerLoserResponseRow buildGainerLoserRow(
      final DimensionAnalysisResultApi dimensionAnalysisResultApi,
      DimNameValueCostEntry costEntry) {
    SummaryGainerLoserResponseRow row = new SummaryGainerLoserResponseRow();
    row.setBaselineValue(costEntry.getBaselineValue());
    row.setCurrentValue(costEntry.getCurrentValue());
    row.setSizeFactor(costEntry.getSizeFactor());
    row.setDimensionName(costEntry.getDimName());
    row.setDimensionValue(costEntry.getDimValue());
    row.setPercentageChange(computePercentageChange(row.getBaselineValue(), row.getCurrentValue()));
    row.setContributionChange(computeContributionChange(row.getBaselineValue(),
        row.getCurrentValue(),
        dimensionAnalysisResultApi.getBaselineTotal(),
        dimensionAnalysisResultApi.getCurrentTotal()));
    row.setContributionToOverallChange(computeContributionToOverallChange(row.getBaselineValue(),
        row.getCurrentValue(),
        dimensionAnalysisResultApi.getBaselineTotal(),
        dimensionAnalysisResultApi.getCurrentTotal()));
    row.setCost(DOUBLE_FORMATTER.format(roundUp(costEntry.getCost())));
    return row;
  }

  public static void buildDiffSummary(final DimensionAnalysisResultApi dimensionAnalysisResultApi,
      List<CubeNode> nodes,
      int targetLevelCount,
      CostFunction costFunction) {
    // If all nodes have a lower level count than targetLevelCount, then it is not necessary to print the summary with
    // height higher than the available level.
    int maxNodeLevelCount = 0;
    for (CubeNode node : nodes) {
      maxNodeLevelCount = Math.max(maxNodeLevelCount, node.getLevel());
    }
    targetLevelCount = Math.min(maxNodeLevelCount, targetLevelCount);

    // Build the header
    Dimensions dimensions = nodes.get(0).getDimensions();
    for (int i = 0; i < dimensions.size(); ++i) {
      dimensionAnalysisResultApi.getDimensions().add(dimensions.get(i));
    }

    // Build the response
    nodes = SummaryResponseTree.sortResponseTree(nodes, targetLevelCount, costFunction);
    //   Build name tag for each row of responses
    Map<CubeNode, NameTag> nameTags = new HashMap<>();
    Map<CubeNode, LinkedHashSet<String>> otherDimensionValues = new HashMap<>();
    for (CubeNode node : nodes) {
      NameTag tag = new NameTag(targetLevelCount);
      nameTags.put(node, tag);
      tag.copyNames(node.getDimensionValues());

      // Put all children name to other dimension values, which will be shown on UI if this node is (ALL)-
      // Later, each picked child will remove itself from this parent's other dimension values.
      LinkedHashSet<String> childrenNames = new LinkedHashSet<>();
      List<CubeNode> children = node.getChildren();
      for (CubeNode child : children) {
        String childName = child.getDimensionValues().get(node.getLevel()).trim();
        if (!childName.isEmpty()) {
          childrenNames.add(child.getDimensionValues().get(node.getLevel()));
        }
      }
      otherDimensionValues.put(node, childrenNames);
    }
    //   pre-condition: parent node is processed before its children nodes
    for (CubeNode node : nodes) {
      CubeNode parent = node;
      int levelDiff = 1;
      while ((parent = parent.getParent()) != null) {
        NameTag parentNameTag = nameTags.get(parent);
        if (parentNameTag != null) {
          // Set parent's name tag from ALL to NOT_ALL String.
          int notAllLevel = node.getLevel() - levelDiff;
          parentNameTag.setNotAll(notAllLevel);
          // After that, set the names after NOT_ALL to empty, e.g., [home page, (ALL)-, ""]
          for (int i = notAllLevel + 1; i < targetLevelCount; ++i) {
            parentNameTag.setEmpty(i);
          }
          // Each picked child will remove itself from this parent's other dimension values.
          // Thus, the (ALL)- node will only show the names of the children name that are NOT picked in the summary.
          otherDimensionValues.get(parent).remove(node.getDimensionValues().get(parent.getLevel()));
          break;
        }
        ++levelDiff;
      }
    }
    //    Fill in the information of each response row
    for (CubeNode node : nodes) {
      SummaryResponseRow row = new SummaryResponseRow();
      row.setNames(nameTags.get(node).getNames());
      row.setBaselineValue(node.getBaselineValue());
      row.setCurrentValue(node.getCurrentValue());
      row.setPercentageChange(computePercentageChange(row.getBaselineValue(),
          row.getCurrentValue()));
      row.setSizeFactor((node.getBaselineSize() + node.getCurrentSize()) / (
          dimensionAnalysisResultApi.getBaselineTotalSize()
              + dimensionAnalysisResultApi.getCurrentTotalSize()));
      row.setContributionChange(computeContributionChange(row.getBaselineValue(),
          row.getCurrentValue(),
          dimensionAnalysisResultApi.getBaselineTotal(),
          dimensionAnalysisResultApi.getCurrentTotal()));
      row.setContributionToOverallChange(computeContributionToOverallChange(row.getBaselineValue(),
          row.getCurrentValue(),
          dimensionAnalysisResultApi.getBaselineTotal(),
          dimensionAnalysisResultApi.getCurrentTotal()));
      row.setCost(node.getCost());
      // Add other dimension values if this node is (ALL)-
      StringBuilder sb = new StringBuilder();
      String separator = "";
      Iterator<String> iterator = otherDimensionValues.get(node).iterator();
      int counter = 0;
      while (iterator.hasNext()) {
        if (++counter > 10) { // Get at most 10 children
          sb.append(separator).append("and more...");
          break;
        }
        sb.append(separator).append(iterator.next());
        separator = ", ";
      }
      row.setOtherDimensionValues(sb.toString());
      dimensionAnalysisResultApi.getResponseRows().add(row);
    }
  }

  public static String computePercentageChange(double baseline, double current) {
    if (baseline != 0d) {
      double percentageChange = ((current - baseline) / baseline) * 100d;
      return DOUBLE_FORMATTER.format(roundUp(percentageChange)) + "%";
    } else {
      return INFINITE;
    }
  }

  public static String computeContributionChange(double baseline, double current,
      double baselineTotal,
      double currentTotal) {
    if (currentTotal != 0d && baselineTotal != 0d) {
      double contributionChange = ((current / currentTotal) - (baseline / baselineTotal)) * 100d;
      return DOUBLE_FORMATTER.format(roundUp(contributionChange)) + "%";
    } else {
      return INFINITE;
    }
  }

  public static String computeContributionToOverallChange(double baseline, double current,
      double baselineTotal,
      double currentTotal) {
    if (baselineTotal != 0d) {
      double contributionToOverallChange =
          ((current - baseline) / Math.abs(currentTotal - baselineTotal)) * 100d;
      return DOUBLE_FORMATTER.format(roundUp(contributionToOverallChange)) + "%";
    } else {
      return INFINITE;
    }
  }

  public static double roundUp(double number) {
    return Math.round(number * 10000d) / 10000d;
  }

  // TODO: Need a better definition for "a node is thinned out by its children."
  // We also need to look into the case where parent node is much smaller than its children.
  private static boolean nodeIsThinnedOut(CubeNode node) {
    return Double.compare(0., node.getBaselineSize()) == 0
        && Double.compare(0., node.getCurrentSize()) == 0;
  }

  private static void rollbackInsertions(CubeNode node, Set<CubeNode> answer,
      List<CubeNode> removedNodes) {
    removedNodes.sort(NODE_COMPARATOR); // Rollback from top to bottom nodes
    Collections.reverse(removedNodes);
    Set<CubeNode> targetSet = new HashSet<>(answer);
    targetSet.addAll(removedNodes);
    for (CubeNode removedNode : removedNodes) {
      CubeNode parents = findAncestor(removedNode, node, targetSet);
      if (parents != null) {
        parents.removeNodeValues(removedNode);
      }
    }
    node.resetValues();
  }

  /**
   * Recompute the baseline value and current value the node. The change is induced by the chosen
   * nodes in
   * the answer. Note that the current node may be in the answer.
   */
  private static void updateWowValues(CubeNode node, Set<CubeNode> answer) {
    node.resetValues();
    for (CubeNode child : answer) {
      if (child == node) {
        continue;
      }
      node.removeNodeValues(child);
    }
  }

  /**
   * Update an internal node's baseline and current values if any of the nodes in its subtree is
   * removed.
   *
   * @param node The internal node to be updated.
   * @param answer The new answer.
   * @param removedNodes The nodes removed from the subtree of node.
   */
  private static void updateWowValuesDueToRemoval(CubeNode node, Set<CubeNode> answer,
      Set<CubeNode> removedNodes) {
    List<CubeNode> removedNodesList = new ArrayList<>(removedNodes);
    removedNodesList.sort(NODE_COMPARATOR); // Process lower level nodes first
    for (CubeNode removedNode : removedNodesList) {
      CubeNode parents = findAncestor(removedNode, node, answer);
      if (parents != null) {
        parents.addNodeValues(removedNode);
      }
    }
  }

  /**
   * Find a node's ancestor between the given node and ceiling that is contained in the target set
   * of CubeNode.
   * Returns null if no ancestor exists in the target set.
   */
  private static CubeNode findAncestor(CubeNode node, CubeNode ceiling, Set<CubeNode> targets) {
    while (node != null && (node = node.getParent()) != ceiling) {
      if (targets.contains(node)) {
        return node;
      }
    }
    return null;
  }

  public static void main(String[] argc) {
    String oFileName = "Cube.json";
    int answerSize = 10;
    boolean doOneSideError = true;
    int maxDimensionSize = 3;

    Cube cube = null;
    try {
      cube = Cube.fromJson(oFileName);
      System.out.println("Restored Cube:");
      System.out.println(cube);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    Summary summary = new Summary(cube, new BalancedCostFunction());
    try {
      DimensionAnalysisResultApi response = summary
          .computeSummary(answerSize, doOneSideError, maxDimensionSize);
      System.out.print("JSon String: ");
      System.out.println(new ObjectMapper().writeValueAsString(response));
      System.out.println("Object String: ");
      System.out.println(response.toString());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    summary.testCorrectnessOfWowValues();
  }

  public DimensionAnalysisResultApi computeSummary(int answerSize) {
    return computeSummary(answerSize, false, this.maxLevelCount);
  }

  public DimensionAnalysisResultApi computeSummary(int answerSize, boolean doOneSideError) {
    return computeSummary(answerSize, doOneSideError, this.maxLevelCount);
  }

  public DimensionAnalysisResultApi computeSummary(int answerSize, int levelCount) {
    return computeSummary(answerSize, false, levelCount);
  }

  public DimensionAnalysisResultApi computeSummary(int answerSize, boolean doOneSideError,
      int userLevelCount) {
    checkArgument(answerSize >= 1, String.format("answerSize is %s. Answer size must be >= 1", answerSize));
    if (userLevelCount <= 0 || userLevelCount > this.maxLevelCount) {
      userLevelCount = this.maxLevelCount;
    }
    this.levelCount = userLevelCount;

    dpArrays = new ArrayList<>(this.levelCount);
    for (int i = 0; i < this.levelCount; ++i) {
      dpArrays.add(new DPArray(answerSize));
    }
    CubeNode root = cube.getRoot();
    if (doOneSideError) {
      oneSideErrorRowInserter =
          new OneSideErrorRowInserter(basicRowInserter,
              Double.compare(1., root.bootStrapChangeRatio()) <= 0);
      // If this cube contains only one dimension, one side error is calculated starting at leaf (detailed) level;
      // otherwise, a row at different side is removed through internal nodes.
      if (this.levelCount == 1) {
        leafRowInserter = oneSideErrorRowInserter;
      }
    }
    computeChildDPArray(root);
    List<CubeNode> answer = new ArrayList<>(dpArrays.get(0).getAnswer());
    DimensionAnalysisResultApi response = new DimensionAnalysisResultApi()
        .setBaselineTotal(cube.getBaselineTotal())
        .setCurrentTotal(cube.getCurrentTotal())
        .setBaselineTotalSize(cube.getBaselineTotalSize())
        .setCurrentTotalSize(cube.getCurrentTotalSize())
        .setGlobalRatio(roundUp(cube.getCurrentTotal() / cube.getBaselineTotal()));

    buildDiffSummary(response, answer, this.levelCount, costFunction);
    buildGainerLoserGroup(response, costSet);
    response.setDimensionCosts(sortedDimensionCosts);

    return response;
  }

  /**
   * Check correctness of the sum of wow values. The check changes the wow values, so it should only
   * be invoked after
   * SummaryResponse is generated.
   */
  public void testCorrectnessOfWowValues() {
    List<CubeNode> nodeList = new ArrayList<>(dpArrays.get(0).getAnswer());
    nodeList.sort(NODE_COMPARATOR); // Process lower level nodes first
    for (CubeNode node : nodeList) {
      CubeNode parent = findAncestor(node, null, dpArrays.get(0).getAnswer());
      if (parent != null) {
        parent.addNodeValues(node);
      }
    }
    for (CubeNode node : nodeList) {
      if (Double.compare(node.getBaselineValue(), node.getOriginalBaselineValue()) != 0
          || Double.compare(node.getCurrentValue(), node.getOriginalCurrentValue()) != 0) {
        LOG.warn("Wrong Wow values at node: " + node.getDimensionValues() + ". Expected: "
            + node.getOriginalBaselineValue() + "," + node.getOriginalCurrentValue() + ", actual: "
            + node.getBaselineValue() + "," + node.getCurrentValue());
      }
    }
  }

  /**
   * Build the summary recursively. The parentTargetRatio for the root node can be any arbitrary
   * value.
   * The calculated answer for each invocation is put at dpArrays[node.level].
   * So, the final answer is located at dpArray[0].
   */
  private void computeChildDPArray(CubeNode node) {
    CubeNode parent = node.getParent();
    DPArray dpArray = dpArrays.get(node.getLevel());
    dpArray.fullReset();
    dpArray.targetRatio = node.bootStrapChangeRatio();

    // Compute DPArray if the current node is the lowest internal node.
    // Otherwise, merge DPArrays from its children.
    if (node.getLevel() == levelCount - 1) {
      // Shrink answer size for getting a higher level view, which gives larger picture of the dataset
      // Uncomment the following block to roll-up rows aggressively
//      if (node.childrenSize() < dpArray.size()) {
//        dpArray.setShrinkSize(Math.max(2, (node.childrenSize()+1)/2));
//      }
      for (CubeNode child : (List<CubeNode>) node.getChildren()) {
        leafRowInserter.insertRowToDPArray(dpArray, child, node.bootStrapChangeRatio());
        updateWowValues(node, dpArray.getAnswer());
        dpArray.targetRatio = node.bootStrapChangeRatio(); // get updated changeRatio
      }
    } else {
      for (CubeNode child : (List<CubeNode>) node.getChildren()) {
        computeChildDPArray(child);
        mergeDPArray(node, dpArray, dpArrays.get(node.getLevel() + 1));
        updateWowValues(node, dpArray.getAnswer());
        dpArray.targetRatio = node.bootStrapChangeRatio(); // get updated changeRatio
      }
      // Use the following block to replace the above one to roll-up rows aggressively
//      List<CubeNode> removedNodes = new ArrayList<>();
//      boolean doRollback = false;
//      do {
//        doRollback = false;
//        for (CubeNode child : node.getChildren()) {
//          computeChildDPArray(child);
//          removedNodes.addAll(mergeDPArray(node, dpArray, dpArrays.get(node.getLevel() + 1)));
//          updateWowValues(node, dpArray.getAnswer());
//          dpArray.bootStrapChangeRatio = node.bootStrapChangeRatio(); // get updated changeRatio
//        }
//        // Aggregate current node's answer if it is thinned out due to the user's answer size is too huge.
//        // If the current node is kept being thinned out, it eventually aggregates all its children.
//        if ( nodeIsThinnedOut(node) && dpArray.getAnswer().size() < dpArray.maxSize()) {
//          doRollback = true;
//          rollbackInsertions(node, dpArray.getAnswer(), removedNodes);
//          removedNodes.clear();
//          dpArray.setShrinkSize(Math.max(1, (dpArray.getAnswer().size()*2)/3));
//          dpArray.reset();
//          dpArray.bootStrapChangeRatio = node.bootStrapChangeRatio();
//        }
//      } while (doRollback);
    }

    // Calculate the cost if the node (aggregated row) is put in the answer.
    // We do not need to do this for the root node.
    // Moreover, if a node is thinned out by its children, it won't be inserted to the answer.
    if (node.getLevel() != 0) {
      updateWowValues(parent, dpArray.getAnswer());
      double targetRatio = parent.bootStrapChangeRatio();
      recomputeCostAndRemoveSmallNodes(node, dpArray, targetRatio);
      dpArray.targetRatio = targetRatio;
      if (!nodeIsThinnedOut(node)) {
        // dpArray actually takes (dpArray.size-1) nodes as the answer, so we set its size to 2
        // in order to insert the aggregated node to the answer.
        if (dpArray.size() == 1) {
          dpArray.setShrinkSize(2);
        }
        Set<CubeNode> removedNode = new HashSet<>(dpArray.getAnswer());
        basicRowInserter.insertRowToDPArray(dpArray, node, targetRatio);
        // The following block is trying to achieve removedNode.removeAll(dpArray.getAnswer());
        // However, removeAll uses equalsTo() instead of equals() to determine if two objects are equal.
        for (CubeNode cubeNode : dpArray.getAnswer()) {
          removedNode.remove(cubeNode);
        }
        if (removedNode.size() != 0) {
          updateWowValuesDueToRemoval(node, dpArray.getAnswer(), removedNode);
          updateWowValues(node, dpArray.getAnswer());
        }
      }
    } else {
      dpArray.getAnswer().add(node);
    }
  }

  /**
   * Merge the answers of the two given DPArrays. The merged answer is put in the DPArray at the
   * left hand side.
   * After merging, the baseline and current values of the removed nodes (rows) will be add back to
   * those of their
   * parent node.
   */
  private Set<CubeNode> mergeDPArray(CubeNode parentNode, DPArray parentArray, DPArray childArray) {
    Set<CubeNode> removedNodes = new HashSet<>(parentArray.getAnswer());
    removedNodes.addAll(childArray.getAnswer());
    // Compute the merged answer
    double targetRatio = (parentArray.targetRatio + childArray.targetRatio) / 2.;
    recomputeCostAndRemoveSmallNodes(parentNode, parentArray, targetRatio);
    List<CubeNode> childNodeList = new ArrayList<>(childArray.getAnswer());
    childNodeList.sort(NODE_COMPARATOR);
    for (CubeNode childNode : childNodeList) {
      insertRowWithAdaptiveRatio(parentArray, childNode, targetRatio);
    }
    // Update an internal node's baseline and current value if any of its child is removed due to the merge
    removedNodes.removeAll(parentArray.getAnswer());
    updateWowValuesDueToRemoval(parentNode, parentArray.getAnswer(), removedNodes);
    return removedNodes;
  }

  /**
   * Recompute costs of the nodes in a DPArray using bootStrapChangeRatio for calculating the cost.
   */
  private void recomputeCostAndRemoveSmallNodes(CubeNode parentNode, DPArray dp,
      double targetRatio) {
    Set<CubeNode> removedNodes = new HashSet<>(dp.getAnswer());
    List<CubeNode> ans = new ArrayList<>(dp.getAnswer());
    ans.sort(NODE_COMPARATOR);
    dp.reset();
    for (CubeNode node : ans) {
      insertRowWithAdaptiveRatioNoOneSideError(dp, node, targetRatio);
    }
    removedNodes.removeAll(dp.getAnswer());
    if (removedNodes.size() != 0) {
      // Temporarily add parentNode to the answer so the baseline and current values of the removed small node can
      // successfully add back to parentNode by re-using the method updateWowValuesDueToRemoval.
      dp.getAnswer().add(parentNode);
      updateWowValuesDueToRemoval(parentNode.getParent(), dp.getAnswer(), removedNodes);
      dp.getAnswer().remove(parentNode);
    }
  }

  /**
   * If the node's parent is also in the DPArray, then it's parent's current changeRatio is used as
   * the target changeRatio for
   * calculating the cost of the node; otherwise, bootStrapChangeRatio is used.
   */
  private void insertRowWithAdaptiveRatioNoOneSideError(DPArray dp, CubeNode node,
      double targetRatio) {
    if (dp.getAnswer().contains(node.getParent())) {
      // For one side error if node's parent is included in the solution, then its cost will be calculated normally.
      basicRowInserter.insertRowToDPArray(dp, node, node.getParent().bootStrapChangeRatio());
    } else {
      basicRowInserter.insertRowToDPArray(dp, node, targetRatio);
    }
  }

  /**
   * If the node's parent is also in the DPArray, then it's parent's current changeRatio is used as
   * the target changeRatio for
   * calculating the cost of the node; otherwise, bootStrapChangeRatio is used.
   */
  private void insertRowWithAdaptiveRatio(DPArray dp, CubeNode node, double targetRatio) {
    if (dp.getAnswer().contains(node.getParent())) {
      // For one side error if node's parent is included in the solution, then its cost will be calculated normally.
      basicRowInserter.insertRowToDPArray(dp, node, node.getParent().bootStrapChangeRatio());
    } else {
      oneSideErrorRowInserter.insertRowToDPArray(dp, node, targetRatio);
    }
  }

  private interface RowInserter {

    void insertRowToDPArray(DPArray dp, CubeNode node, double targetRatio);
  }

  static class NodeDimensionValuesComparator implements Comparator<CubeNode> {

    @Override
    public int compare(CubeNode n1, CubeNode n2) {
      return n1.getDimensionValues().compareTo(n2.getDimensionValues());
    }
  }

  /**
   * A wrapper class over BasicRowInserter. This class provide the calculation for one side error
   * summary.
   */
  private static class OneSideErrorRowInserter implements RowInserter {

    final RowInserter basicRowInserter;
    final boolean side;

    public OneSideErrorRowInserter(RowInserter basicRowInserter, boolean side) {
      this.basicRowInserter = basicRowInserter;
      this.side = side;
    }

    @Override
    public void insertRowToDPArray(DPArray dp, CubeNode node, double targetRatio) {
      // If the row has the same change trend with the top row, then it is inserted.
      if (side == node.side()) {
        basicRowInserter.insertRowToDPArray(dp, node, targetRatio);
      } else { // Otherwise, it is inserted only there exists an intermediate parent besides root node
        CubeNode parent = findAncestor(node, null, dp.getAnswer());
        if (parent != null && parent.side() == side) {
          basicRowInserter.insertRowToDPArray(dp, node, targetRatio);
        }
      }
    }
  }

  private class BasicRowInserter implements RowInserter {

    private final CostFunction costFunction;

    public BasicRowInserter(CostFunction costFunction) {
      this.costFunction = costFunction;
    }

    @Override
    public void insertRowToDPArray(DPArray dp, CubeNode node, double targetRatio) {
      double baselineValue = node.getBaselineValue();
      double currentValue = node.getCurrentValue();
      double baselineSize = node.getBaselineSize();
      double currentSize = node.getCurrentSize();
      double cost = costFunction.computeCost(targetRatio,
          baselineValue,
          currentValue,
          baselineSize,
          currentSize,
          globalBaselineValue,
          globalCurrentValue,
          globalBaselineSize,
          globalCurrentSize);

      for (int n = dp.size() - 1; n > 0; --n) {
        double val1 = dp.slotAt(n - 1).cost;
        double val2 = dp.slotAt(n).cost + cost; // fixed r per iteration
        if (Double.compare(val1, val2) < 0) {
          dp.slotAt(n).cost = val1;
          dp.slotAt(n).ans.retainAll(dp.slotAt(n - 1).ans); // dp[n].ans = dp[n-1].ans
          dp.slotAt(n).ans.add(node);
        } else {
          dp.slotAt(n).cost = val2;
        }
      }
      dp.slotAt(0).cost = dp.slotAt(0).cost + cost;
    }
  }
}
