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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.pinot.thirdeye.cube.cost.CostFunction;
import org.apache.pinot.thirdeye.cube.data.cube.Cube;
import org.apache.pinot.thirdeye.cube.data.cube.DimNameValueCostEntry;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.data.node.CubeNode;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.api.cube.SummaryGainerLoserResponseRow;
import org.apache.pinot.thirdeye.spi.api.cube.SummaryResponseRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Summary {

  public final static int MAX_GAINER_LOSER_COUNT = 5;
  public final static NumberFormat DOUBLE_FORMATTER = new DecimalFormat("#0.0000");
  public static final String INFINITE = "";

  static final NodeDimensionValuesComparator NODE_COMPARATOR = new NodeDimensionValuesComparator();
  private static final Logger LOG = LoggerFactory.getLogger(Summary.class);

  private final Cube cube;
  private final CostFunction costFunction;
  private final RowInserter basicRowInserter;
  private RowInserter oneSideErrorRowInserter;
  private RowInserter leafRowInserter;

  public Summary(Cube cube, CostFunction costFunction) {
    this.cube = cube;
    this.costFunction = costFunction;

    // fixme cyril I'm here - clean the row Inserter design
    // fixme cyril  + move api generation to DataCubeSummaryCalculator ?
    this.basicRowInserter = new BasicRowInserter(costFunction, cube);
    this.oneSideErrorRowInserter = basicRowInserter;
    this.leafRowInserter = basicRowInserter;
  }

  private void buildGainerLoserGroup(final DimensionAnalysisResultApi dimensionAnalysisResultApi) {
    for (DimNameValueCostEntry dimNameValueCostEntry : cube.getCostSet()) {
      if (Double.compare(dimNameValueCostEntry.getCost(), 0) <= 0) {
        // skip when cost is negligible
        continue;
      }
      if (dimNameValueCostEntry.getCurrentValue() >= dimNameValueCostEntry.getBaselineValue()
          && dimensionAnalysisResultApi.getGainer().size() < MAX_GAINER_LOSER_COUNT) {
        dimensionAnalysisResultApi.getGainer().add(
            buildGainerLoserRow(dimNameValueCostEntry,
                dimensionAnalysisResultApi.getBaselineTotal(),
                dimensionAnalysisResultApi.getCurrentTotal()));
      } else if (dimNameValueCostEntry.getCurrentValue() < dimNameValueCostEntry.getBaselineValue()
          && dimensionAnalysisResultApi.getLoser().size() < MAX_GAINER_LOSER_COUNT) {
        dimensionAnalysisResultApi.getLoser().add(
            buildGainerLoserRow(dimNameValueCostEntry,
                dimensionAnalysisResultApi.getBaselineTotal(),
                dimensionAnalysisResultApi.getCurrentTotal()));
      }
      if (dimensionAnalysisResultApi.getGainer().size() >= MAX_GAINER_LOSER_COUNT
          && dimensionAnalysisResultApi.getLoser().size() >= MAX_GAINER_LOSER_COUNT) {
        break;
      }
    }
  }

  private static SummaryGainerLoserResponseRow buildGainerLoserRow(
      final DimNameValueCostEntry costEntry, final double baselineTotal,
      final double currentTotal) {
    SummaryGainerLoserResponseRow row = new SummaryGainerLoserResponseRow();
    row.setBaselineValue(costEntry.getBaselineValue());
    row.setCurrentValue(costEntry.getCurrentValue());
    row.setSizeFactor(costEntry.getSizeFactor());
    row.setDimensionName(costEntry.getDimName());
    row.setDimensionValue(costEntry.getDimValue());
    row.setPercentageChange(computePercentageChange(row.getBaselineValue(), row.getCurrentValue()));
    row.setContributionChange(computeContributionChange(row.getBaselineValue(),
        row.getCurrentValue(),
        baselineTotal,
        currentTotal));
    row.setContributionToOverallChange(computeContributionToOverallChange(row.getBaselineValue(),
        row.getCurrentValue(),
        baselineTotal,
        currentTotal));
    row.setCost(DOUBLE_FORMATTER.format(roundUp(costEntry.getCost())));
    return row;
  }

  public static void buildDiffSummary(final DimensionAnalysisResultApi dimensionAnalysisResultApi,
      List<CubeNode> nodes,
      CostFunction costFunction) {
    // If all nodes have a lower level count than targetLevelCount, then it is not necessary to print the summary with
    // height higher than the available level.
    int maxNodeLevelCount = 0;
    for (CubeNode node : nodes) {
      maxNodeLevelCount = Math.max(maxNodeLevelCount, node.getLevel());
    }

    // Build the header
    Dimensions dimensions = nodes.get(0).getDimensions();
    dimensionAnalysisResultApi.getDimensions().addAll(dimensions.names());

    // Build the response
    nodes = SummaryResponseTree.sortResponseTree(nodes, maxNodeLevelCount, costFunction);
    //   Build name tag for each row of responses
    Map<CubeNode, NameTag> nameTags = new HashMap<>();
    Map<CubeNode, LinkedHashSet<String>> otherDimensionValues = new HashMap<>();
    for (CubeNode node : nodes) {
      NameTag tag = new NameTag(maxNodeLevelCount);
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
          for (int i = notAllLevel + 1; i < maxNodeLevelCount; ++i) {
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

  /**
   * Recompute the baseline value and current value the node. The change is induced by the chosen
   * nodes in the answer. Note that the current node may be in the answer.
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

  public DimensionAnalysisResultApi computeSummary(int answerSize, boolean doOneSideError) {
    return computeSummary(answerSize, doOneSideError, cube.getDimensions().size());
  }

  public DimensionAnalysisResultApi computeSummary(int answerSize, boolean doOneSideError,
      int levelCount) {
    checkArgument(answerSize >= 1,
        String.format("answerSize is %s. Answer size must be >= 1", answerSize));
    if (levelCount <= 0 || levelCount > cube.getDimensions().size()) {
      levelCount = cube.getDimensions().size();
    }

    CubeNode root = cube.getRoot();
    if (doOneSideError) {
      oneSideErrorRowInserter =
          new OneSideErrorRowInserter(basicRowInserter,
              Double.compare(1., root.bootStrapChangeRatio()) <= 0);
      // If this cube contains only one dimension, one side error is calculated starting at leaf (detailed) level;
      // otherwise, a row at different side is removed through internal nodes.
      if (levelCount == 1) {
        leafRowInserter = oneSideErrorRowInserter;
      }
    }
    final List<DPArray> dpArrays = Stream.generate(() -> new DPArray(answerSize))
        .limit(levelCount).collect(Collectors.toList());
    computeChildDPArray(root, levelCount, dpArrays);
    List<CubeNode> answer = new ArrayList<>(dpArrays.get(0).getAnswer());

    DimensionAnalysisResultApi response = buildApiResponse(answer);

    return response;
  }

  private DimensionAnalysisResultApi buildApiResponse(final List<CubeNode> answer) {
    // build general info
    DimensionAnalysisResultApi response = new DimensionAnalysisResultApi()
        .setBaselineTotal(cube.getBaselineTotal())
        .setCurrentTotal(cube.getCurrentTotal())
        .setBaselineTotalSize(cube.getBaselineTotalSize())
        .setCurrentTotalSize(cube.getCurrentTotalSize())
        .setGlobalRatio(roundUp(cube.getCurrentTotal() / cube.getBaselineTotal()))
        .setDimensionCosts(cube.getSortedDimensionCosts());

    // build response rows
    buildDiffSummary(response, answer, costFunction);
    // build gainer/loser lists
    buildGainerLoserGroup(response);
    return response;
  }

  /**
   * Build the summary recursively. The parentTargetRatio for the root node can be any arbitrary
   * value.
   * The calculated answer for each invocation is put at dpArrays[node.level].
   * dpArrays should be correctly initialized with n=levelCount  DpArray of size answerSize
   * So, the final answer is located at dpArray[0].
   */
  private void computeChildDPArray(CubeNode node, final int levelCount, final List<DPArray> dpArrays) {
    CubeNode parent = node.getParent();
    DPArray dpArray = dpArrays.get(node.getLevel());
    dpArray.fullReset();
    dpArray.targetRatio = node.bootStrapChangeRatio();

    // Compute DPArray if the current node is the lowest internal node.
    // Otherwise, merge DPArrays from its children.
    if (node.getLevel() == levelCount - 1) {
      // Shrink answer size for getting a higher level view, which gives larger picture of the dataset
      // GIT REF - roll-up rows aggressively code suggestion removed - see previous commit
      for (CubeNode child : (List<CubeNode>) node.getChildren()) {
        leafRowInserter.insertRowToDPArray(dpArray, child, node.bootStrapChangeRatio());
        updateWowValues(node, dpArray.getAnswer());
        dpArray.targetRatio = node.bootStrapChangeRatio(); // get updated changeRatio
      }
    } else {
      for (CubeNode child : (List<CubeNode>) node.getChildren()) {
        computeChildDPArray(child, levelCount, dpArrays);
        mergeDPArray(node, dpArray, dpArrays.get(node.getLevel() + 1));
        updateWowValues(node, dpArray.getAnswer());
        dpArray.targetRatio = node.bootStrapChangeRatio(); // get updated changeRatio
      }
      // GIT REF - roll-up rows aggressively code suggestion removed - see previous commit
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

  private static class BasicRowInserter implements RowInserter {

    private final double globalBaselineValue;
    private final double globalCurrentValue;
    private final double globalBaselineSize;
    private final double globalCurrentSize;
    private final CostFunction costFunction;

    public BasicRowInserter(CostFunction costFunction, final Cube cube) {
      this.costFunction = costFunction;
      this.globalBaselineValue = cube.getBaselineTotal();
      this.globalCurrentValue = cube.getCurrentTotal();
      this.globalBaselineSize = cube.getBaselineTotalSize();
      this.globalCurrentSize = cube.getCurrentTotalSize();
    }

    @Override
    public void insertRowToDPArray(DPArray dp, CubeNode node, double targetRatio) {
      double cost = costFunction.computeCost(targetRatio,
          node.getBaselineValue(),
          node.getCurrentValue(),
          node.getBaselineSize(),
          node.getCurrentSize(),
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
