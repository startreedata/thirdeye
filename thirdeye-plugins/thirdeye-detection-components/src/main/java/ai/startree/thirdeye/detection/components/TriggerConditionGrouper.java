/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */
package ai.startree.thirdeye.detection.components;

import static ai.startree.thirdeye.spi.detection.DetectionUtils.makeParentEntityAnomaly;
import static ai.startree.thirdeye.spi.detection.DetectionUtils.mergeAndSortAnomalies;
import static ai.startree.thirdeye.spi.detection.DetectionUtils.setEntityChildMapping;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.Grouper;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expression based grouper - supports AND, OR and nested combinations of grouping
 */
public class TriggerConditionGrouper implements Grouper<TriggerConditionGrouperSpec> {

  protected static final Logger LOG = LoggerFactory.getLogger(TriggerConditionGrouper.class);

  static final String PROP_AND = "and";
  static final String PROP_OR = "or";
  static final String PROP_OPERATOR = "operator";
  static final String PROP_LEFT_OP = "leftOp";
  static final String PROP_RIGHT_OP = "rightOp";

  private static final String PROP_SUB_ENTITY_NAME = "subEntityName";

  private String expression;
  private String operator;
  private Map<String, Object> leftOp;
  private Map<String, Object> rightOp;

  @Override
  public void init(TriggerConditionGrouperSpec spec) {
    this.expression = spec.getExpression();
    this.operator = spec.getOperator();
    this.leftOp = spec.getLeftOp();
    this.rightOp = spec.getRightOp();
  }

  @Override
  public void init(TriggerConditionGrouperSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
  }

  /**
   * Group based on 'AND' criteria - Entity has anomaly if both sub-entities A and B have anomalies
   * at the same time. This means we find anomaly overlapping interval.
   *
   * Since the anomalies from the respective entities/metrics are merged
   * before calling the grouper, we do not have to deal with overlapping
   * anomalies within an entity/metric
   *
   * Sort anomalies and incrementally compare two anomalies for overlap criteria; break when no
   * overlap
   */
  private List<MergedAnomalyResultDTO> andGrouping(
      List<MergedAnomalyResultDTO> anomalyListA, List<MergedAnomalyResultDTO> anomalyListB) {
    Set<MergedAnomalyResultDTO> groupedAnomalies = new HashSet<>();
    List<MergedAnomalyResultDTO> anomalies = mergeAndSortAnomalies(anomalyListA, anomalyListB);
    if (anomalies.isEmpty()) {
      return anomalies;
    }

    for (int i = 0; i < anomalies.size(); i++) {
      for (int j = i + 1; j < anomalies.size(); j++) {
        // Check for overlap and output it
        if (anomalies.get(j).getStartTime() <= anomalies.get(i).getEndTime()) {
          MergedAnomalyResultDTO currentAnomaly = makeParentEntityAnomaly(anomalies.get(i));
          currentAnomaly
              .setEndTime(Math.min(currentAnomaly.getEndTime(), anomalies.get(j).getEndTime()));
          currentAnomaly.setStartTime(anomalies.get(j).getStartTime());
          setEntityChildMapping(currentAnomaly, anomalies.get(j));

          groupedAnomalies.add(currentAnomaly);
        } else {
          break;
        }
      }
    }

    return new ArrayList<>(groupedAnomalies);
  }

  /**
   * Group based on 'OR' criteria - Entity has anomaly if either sub-entity A or B have anomalies.
   * This means we find the total anomaly coverage.
   *
   * Since the anomalies from the respective entities/metrics are merged
   * before calling the grouper, we do not have to deal with overlapping
   * anomalies within an entity/metric
   *
   * Sort anomalies by start time and incrementally merge anomalies
   */
  private List<MergedAnomalyResultDTO> orGrouping(
      List<MergedAnomalyResultDTO> anomalyListA, List<MergedAnomalyResultDTO> anomalyListB) {
    Set<MergedAnomalyResultDTO> groupedAnomalies = new HashSet<>();
    List<MergedAnomalyResultDTO> anomalies = mergeAndSortAnomalies(anomalyListA, anomalyListB);
    if (anomalies.isEmpty()) {
      return anomalies;
    }

    MergedAnomalyResultDTO currentAnomaly = makeParentEntityAnomaly(anomalies.get(0));
    for (int i = 1; i < anomalies.size(); i++) {
      if (anomalies.get(i).getStartTime() <= currentAnomaly.getEndTime()) {
        // Partial or full overlap
        currentAnomaly
            .setEndTime(Math.max(anomalies.get(i).getEndTime(), currentAnomaly.getEndTime()));
        setEntityChildMapping(currentAnomaly, anomalies.get(i));
      } else {
        // No overlap
        groupedAnomalies.add(currentAnomaly);
        currentAnomaly = makeParentEntityAnomaly(anomalies.get(i));
      }
    }
    groupedAnomalies.add(currentAnomaly);

    return new ArrayList<>(groupedAnomalies);
  }

  /**
   * Groups the anomalies based on the parsed operator tree
   */
  private List<MergedAnomalyResultDTO> groupAnomaliesByOperator(Map<String, Object> operatorNode,
      List<MergedAnomalyResultDTO> anomalies) {
    Preconditions.checkNotNull(operatorNode);

    // Base condition - If reached leaf node of operator tree, then return the anomalies corresponding to the entity/metric
    String value = getString(operatorNode, "value");
    if (value != null) {
      return anomalies.stream().filter(anomaly ->
          anomaly.getProperties() != null
              && anomaly.getProperties().containsKey(PROP_SUB_ENTITY_NAME)
              && anomaly.getProperties()
              .get(PROP_SUB_ENTITY_NAME)
              .equals(value)
      ).collect(Collectors.toList());
    }

    String operator = getString(operatorNode, PROP_OPERATOR);
    Preconditions.checkNotNull(operator, "No operator provided!");
    Map<String, Object> leftOp = ConfigUtils.getMap(operatorNode.get(PROP_LEFT_OP));
    Map<String, Object> rightOp = ConfigUtils.getMap(operatorNode.get(PROP_RIGHT_OP));

    // Post-order traversal - find anomalies from left subtree and right sub-tree and then group them
    List<MergedAnomalyResultDTO> leftAnomalies = groupAnomaliesByOperator(leftOp, anomalies);
    List<MergedAnomalyResultDTO> rightAnomalies = groupAnomaliesByOperator(rightOp, anomalies);
    if (operator.equalsIgnoreCase(PROP_AND)) {
      return andGrouping(leftAnomalies, rightAnomalies);
    } else if (operator.equalsIgnoreCase(PROP_OR)) {
      return orGrouping(leftAnomalies, rightAnomalies);
    } else {
      throw new RuntimeException("Unsupported operator");
    }
  }

  private String getString(final Map<String, Object> operatorNode, final String value) {
    return optional(operatorNode.get(value))
        .map(Object::toString)
        .orElse(null);
  }

  @Override
  public List<MergedAnomalyResultDTO> group(List<MergedAnomalyResultDTO> anomalies) {
    Map<String, Object> operatorTreeRoot = new HashMap<>();
    if (operator != null) {
      operatorTreeRoot.put(PROP_OPERATOR, operator);
      operatorTreeRoot.put(PROP_LEFT_OP, leftOp);
      operatorTreeRoot.put(PROP_RIGHT_OP, rightOp);
    } else {
      operatorTreeRoot = ExpressionParser.generateOperators(expression);
    }
    return groupAnomaliesByOperator(operatorTreeRoot, anomalies);
  }
}
