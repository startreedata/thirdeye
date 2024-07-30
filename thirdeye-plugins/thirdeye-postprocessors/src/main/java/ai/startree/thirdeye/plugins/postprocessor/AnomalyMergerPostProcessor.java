/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static ai.startree.thirdeye.spi.rca.Stats.computeValueChangePercentage;
import static ai.startree.thirdeye.spi.util.AnomalyUtils.isIgnore;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.PostProcessingContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.annotations.VisibleForTesting;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merge anomalies.
 * Note:
 * If used with dimension exploration, must be run inside each enumeration.
 * The current implementation should not be run after the forkjoin node, it would merge anomalies
 * from different enumerations.
 * The algorithm assumes all operatorResult anomalies are of unit size (detection granularity size).
 */
public class AnomalyMergerPostProcessor implements AnomalyPostProcessor {

  public static final String NEW_AFTER_REPLAY_LABEL_NAME = "NEW_AFTER_REPLAY";
  public static final String OUTDATED_AFTER_REPLAY_LABEL_NAME = "OUTDATED_AFTER_REPLAY";

  // by default, only merge consecutive anomalies. And P0D is a special value to disable merging entirely
  @VisibleForTesting
  protected static final Period DEFAULT_MERGE_MAX_GAP = Period.seconds(1);

  @VisibleForTesting
  protected static final Period DEFAULT_ANOMALY_MAX_DURATION = Period.days(7);

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyMergerPostProcessor.class);

  private static final String NAME = "ANOMALY_MERGER";
  private static final double DEFAULT_RENOTIFY_PERCENTAGE_THRESHOLD = -1;
  private static final double DEFAULT_RENOTIFY_ABSOLUTE_THRESHOLD = -1;
  private static final Comparator<AnomalyDTO> COMPARATOR = (o1, o2) -> {
    // smallest startTime is smaller
    int res = Long.compare(o1.getStartTime(), o2.getStartTime());
    if (res != 0) {
      return res;
    }

    // biggest endTime is smaller
    res = -1 * Long.compare(o1.getEndTime(), o2.getEndTime());
    if (res != 0) {
      return res;
    }

    // pre-existing
    if (o1.getId() == null && o2.getId() != null) {
      return 1;
    }
    if (o1.getId() != null && o2.getId() == null) {
      return -1;
    }

    // more children first
    return -1 * Integer.compare(o1.getChildren().size(), o2.getChildren().size());
  };
  private static final Set<String> REPLAY_LABELS = Set.of(
      NEW_AFTER_REPLAY_LABEL_NAME,
      OUTDATED_AFTER_REPLAY_LABEL_NAME
  );

  private final Period mergeMaxGap;
  private final Period mergeMaxDuration;
  private final @Nullable Long alertId;
  private final @Nullable EnumerationItemDTO enumerationItem;
  private final DetectionPipelineUsage usage;
  private final AnomalyManager anomalyManager;
  private final double reNotifyPercentageThreshold;
  private final double reNotifyAbsoluteThreshold;

  // obtained at runtime
  private Chronology chronology;

  public AnomalyMergerPostProcessor(final AnomalyMergerPostProcessorSpec spec) {
    mergeMaxGap = isoPeriod(spec.getMergeMaxGap(), DEFAULT_MERGE_MAX_GAP);
    mergeMaxDuration = isoPeriod(spec.getMergeMaxDuration(), DEFAULT_ANOMALY_MAX_DURATION);
    alertId = spec.getAlertId();
    usage = spec.getUsage();
    enumerationItem = spec.getEnumerationItemDTO();
    reNotifyPercentageThreshold = optional(spec.getReNotifyPercentageThreshold())
        .orElse(DEFAULT_RENOTIFY_PERCENTAGE_THRESHOLD);
    reNotifyAbsoluteThreshold = optional(spec.getReNotifyAbsoluteThreshold())
        .orElse(DEFAULT_RENOTIFY_ABSOLUTE_THRESHOLD);

    // TODO spyne Refactor. persistence layer should not be accessible here. And definitely not
    //  through spec
    anomalyManager = spec.getAnomalyManager();
  }

  private static boolean hasOutdatedLabel(final AnomalyDTO child) {
    return optional(child.getAnomalyLabels()).orElse(Collections.emptyList())
        .stream()
        .anyMatch(l -> l.getName().equals(OUTDATED_AFTER_REPLAY_LABEL_NAME));
  }

  @VisibleForTesting
  protected static List<AnomalyDTO> combineAndSort(
      final List<AnomalyDTO> anomalies,
      final List<AnomalyDTO> existingAnomalies) {
    final List<AnomalyDTO> generatedAndExistingAnomalies = new ArrayList<>();
    generatedAndExistingAnomalies.addAll(anomalies);
    generatedAndExistingAnomalies.addAll(existingAnomalies);

    // prepare a sorted list for processing
    generatedAndExistingAnomalies.sort(COMPARATOR);
    return generatedAndExistingAnomalies;
  }

  private static void addReplayLabel(final AnomalyDTO anomaly, final AnomalyLabelDTO label) {
    final List<AnomalyLabelDTO> labels = optional(anomaly.getAnomalyLabels()).orElse(
        new ArrayList<>());
    anomaly.setAnomalyLabels(labels);
    labels.removeIf(l -> REPLAY_LABELS.contains(l.getName()));
    labels.add(label);
  }

  @VisibleForTesting
  protected static AnomalyLabelDTO newAfterReplayLabel() {
    return new AnomalyLabelDTO().setName(NEW_AFTER_REPLAY_LABEL_NAME).setIgnore(false);
  }

  @VisibleForTesting
  protected static AnomalyLabelDTO newOutdatedLabel() {
    return new AnomalyLabelDTO().setName(OUTDATED_AFTER_REPLAY_LABEL_NAME).setIgnore(true);
  }

  private static boolean startEndEquals(final AnomalyDTO a, final AnomalyDTO b) {
    return a.getStartTime() == b.getStartTime() && a.getEndTime() == b.getEndTime();
  }

  private static String patternKey(final AnomalyDTO anomaly) {
    String patternKey = "";
    if (!Double.isNaN(anomaly.getAvgBaselineVal()) && !Double.isNaN(anomaly.getAvgCurrentVal())) {
      patternKey = (anomaly.getAvgCurrentVal() > anomaly.getAvgBaselineVal()) ? "UP" : "DOWN";
    }
    return patternKey;
  }

  private static void mergeIntoParent(final AnomalyDTO parent, final AnomalyDTO child) {
    // fully merge into existing
    final Set<AnomalyDTO> children = parent.getChildren();

    // if this parent has no children, then add a copy of itself as a child
    if (children.isEmpty()) {
      children.add(copyAnomalyInfo(parent));
    }
    // Extend the end time to match the child anomaly end time.
    parent.setEndTime(Math.max(parent.getEndTime(), child.getEndTime()));

    // merge the anomaly's properties into parent
    mergeAnomalyProperties(parent.getProperties(), child.getProperties());

    // merge the anomaly labels
    final List<AnomalyLabelDTO> mergedAnomalyLabels = mergeAnomalyLabels(
        // use Arrays.asList because List.of does not allow null values 
        Arrays.asList(child.getAnomalyLabels(), parent.getAnomalyLabels()));
    parent.setAnomalyLabels(mergedAnomalyLabels);

    // merge the anomaly severity
    if (parent.getSeverityLabel().compareTo(child.getSeverityLabel()) > 0) {
      // set the highest severity
      parent.setSeverityLabel(child.getSeverityLabel());
    }

    // If anomaly is a child anomaly, add itself else add all its children
    if (child.getChildren().isEmpty()) {
      children.add(child);
    } else {
      children.addAll(child.getChildren());
    }
  }

  private static AnomalyDTO copyAnomalyInfo(final AnomalyDTO from) {
    final AnomalyDTO to = new AnomalyDTO();
    to.setAuth(from.getAuth());
    to.setStartTime(from.getStartTime());
    to.setEndTime(from.getEndTime());
    to.setMetric(from.getMetric());
    to.setCollection(from.getCollection());
    to.setDetectionConfigId(from.getDetectionConfigId());
    to.setAnomalyResultSource(from.getAnomalyResultSource());
    to.setAvgBaselineVal(from.getAvgBaselineVal());
    to.setAvgCurrentVal(from.getAvgCurrentVal());
    to.setLowerBound(from.getLowerBound());
    to.setUpperBound(from.getUpperBound());
    to.setFeedback(from.getFeedback());
    to.setAnomalyFeedbackId(from.getAnomalyFeedbackId());
    to.setScore(from.getScore());
    to.setWeight(from.getWeight());
    to.setProperties(from.getProperties());
    to.setSeverityLabel(from.getSeverityLabel());
    to.setAnomalyLabels(from.getAnomalyLabels());
    // FIXME CYRIL BEFORE MERGE NOT SURE IF THIS WORKS FINE ANYMORE
    optional(from.getEnumerationItem())
        .map(AnomalyMergerPostProcessor::cloneEnumerationRef)
        .ifPresent(to::setEnumerationItem);
    // child and id not set on purpose
    return to;
  }

  private static void updateAnomalyWithNewValues(final AnomalyDTO currentA, final AnomalyDTO newA) {
    currentA.setAvgBaselineVal(newA.getAvgBaselineVal());
    currentA.setAvgCurrentVal(newA.getAvgCurrentVal());
    currentA.setUpperBound(newA.getUpperBound());
    currentA.setLowerBound(newA.getLowerBound());
    currentA.setScore(newA.getScore());
  }

  /**
   * Merge child's properties into parent's properties.
   * If the property exists in both then use parent's property.
   * For property = "detectorComponentName", combine the parent and child.
   *
   * @param parent The parent anomaly's properties.
   * @param child The child anomaly's properties.
   */
  private static void mergeAnomalyProperties(final Map<String, String> parent,
      final Map<String, String> child) {
    for (final Entry<String, String> e : child.entrySet()) {
      final String key = e.getKey();
      if (!parent.containsKey(key)) {
        parent.put(key, e.getValue());
      }
    }
  }

  private static EnumerationItemDTO cloneEnumerationRef(final EnumerationItemDTO ei) {
    final var clone = new EnumerationItemDTO();
    clone.setId(requireNonNull(ei.getId(), "enumeration item id is null"));
    return clone;
  }

  private static @Nullable List<AnomalyLabelDTO> mergeAnomalyLabels(final List<List<AnomalyLabelDTO>> labelsList) {
    // simple merging logic based on hash - can be enhanced later
    final HashSet<AnomalyLabelDTO> mergedLabels = new HashSet<>();
    for (final List<AnomalyLabelDTO> e: labelsList) {
      if (e != null) {
        mergedLabels.addAll(e); 
      }
    }
    if (mergedLabels.isEmpty()) {
      return null;
    }
    
    return new ArrayList<>(mergedLabels);
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) {
    if (mergeMaxGap.equals(Period.ZERO)) {
      // short-circuit - merging is disabled
      return resultMap;
    }

    chronology = detectionInterval.getChronology();
    resultMap.values()
        .forEach(operatorResult -> postProcessResult(operatorResult, detectionInterval));

    return resultMap;
  }

  private void postProcessResult(final OperatorResult operatorResult,
      final Interval detectionInterval) {
    final List<AnomalyDTO> operatorAnomalies = operatorResult.getAnomalies();
    if (operatorAnomalies == null) {
      return;
    }

    final Set<AnomalyDTO> mergedAnomalies = merge(operatorAnomalies, detectionInterval);
    // TODO spyne Refactor. remove hack - operatorResult is not mutable - exploit list mutability
    //  to update the anomalies could silently bug if a protective copy is returned by an
    //  OperatorResult implementation
    // ensure no protective copy is done comparing references
    checkArgument(operatorAnomalies == operatorResult.getAnomalies());
    operatorAnomalies.clear();
    operatorAnomalies.addAll(mergedAnomalies);
  }

  protected Set<AnomalyDTO> merge(final List<AnomalyDTO> operatorAnomalies,
      final Interval detectionInterval) {
    final List<AnomalyDTO> persistenceAnomalies = retrieveRelevantAnomaliesFromDatabase(
        detectionInterval);
    
    final Set<AnomalyDTO> anomaliesToUpdate = vanishedAnomalies(operatorAnomalies,
        persistenceAnomalies,
        detectionInterval);
    if (!anomaliesToUpdate.isEmpty()) {
      // exclude vanished anomalies from merge operation
      // need to wrap in the emptiness test, because ArrayList.removeAll does not check if the collection is empty and loops through all elements if the input collection is empty
      // TODO CYRIL -- all these array/set manipulations could be removed by merging some of the functions - for the moment focus is on readibility
      persistenceAnomalies.removeAll(anomaliesToUpdate); 
    }
    
    final List<AnomalyDTO> mergedAnomalies = doMerge(operatorAnomalies, persistenceAnomalies);
    anomaliesToUpdate.addAll(mergedAnomalies);

    return anomaliesToUpdate;
  }

  /**
   * Vanished anomalies are anomalies that exist in the persistence db but are not detected
   * anymore. They are tagged as outdated.
   * Notes:
   * A parent anomaly that has a child anomaly vanished, such that the gap between 2 child anomalies
   * is now bigger than mergeMaxGap, is not split into 2 parents.
   * We consider this is an edge case, and in this case the parent anomaly is still valid.
   */
  private Set<AnomalyDTO> vanishedAnomalies(final List<AnomalyDTO> operatorAnomalies,
      final List<AnomalyDTO> persistenceAnomalies,
      final Interval detectionInterval) {
    // using LinkedHashSet is a convenience to make test and debugging simpler
    final Set<AnomalyDTO> vanishedAnomalies = new LinkedHashSet<>(); 
    // first loop looks at the unitary anomalies (child or parent with no children)
    // check if unitary anomalies are found in the latest detection run, based on anomaly startTime
    final Set<Long> operatorAnomaliesStartTimes = operatorAnomalies.stream()
        .map(AnomalyDTO::getStartTime)
        .collect(Collectors.toSet());
    for (final AnomalyDTO existingAnomaly : persistenceAnomalies) {
      final boolean isUnitaryAnomaly = existingAnomaly.isChild()
          || existingAnomaly.getChildren() == null
          || existingAnomaly.getChildren().isEmpty();
      final boolean isInDetectionInterval =
          existingAnomaly.getStartTime() >= detectionInterval.getStartMillis()
              && existingAnomaly.getEndTime() <= detectionInterval.getEndMillis();
      final boolean isInOperatorAnomalies = operatorAnomaliesStartTimes.contains(
          existingAnomaly.getStartTime());
      if (isUnitaryAnomaly && isInDetectionInterval && !isInOperatorAnomalies) {
        // anomaly is outdated - it was not detected in the most recent run
        addReplayLabel(existingAnomaly, newOutdatedLabel());
        vanishedAnomalies.add(existingAnomaly);
      }
    }
    // second loop looks at the parents with children
    // if a parent has all its children tagged as outdated, it is tagged as outdated
    for (final AnomalyDTO existingAnomaly : persistenceAnomalies) {
      final Set<AnomalyDTO> children = existingAnomaly.getChildren();
      if (children != null && !children.isEmpty()) {
        final int numChildrenOutdated = children.stream()
            .map(AnomalyMergerPostProcessor::hasOutdatedLabel)
            .mapToInt(o -> o ? 1 : 0)
            .sum();
        if (numChildrenOutdated == children.size()) {
          // parent is fully outdated
          addReplayLabel(existingAnomaly, newOutdatedLabel());
          vanishedAnomalies.add(existingAnomaly);
        } else if (numChildrenOutdated > 0) {
          // parent is partially outdated - updated bounds
          final List<AnomalyDTO> sortedNotOutdatedChildren = children.stream()
              .filter(a -> !hasOutdatedLabel(a))
              .sorted(COMPARATOR)
              .toList();
          final AnomalyDTO firstChildren = sortedNotOutdatedChildren.get(0);
          final AnomalyDTO lastChildren = sortedNotOutdatedChildren.get(
              sortedNotOutdatedChildren.size() - 1);
          existingAnomaly.setStartTime(firstChildren.getStartTime());
          updateAnomalyWithNewValues(existingAnomaly, firstChildren);
          final List<List<AnomalyLabelDTO>> notOutdatedLabels = sortedNotOutdatedChildren.stream()
              .map(AnomalyDTO::getAnomalyLabels).toList();
          existingAnomaly.setAnomalyLabels(mergeAnomalyLabels(notOutdatedLabels));
          existingAnomaly.setEndTime(lastChildren.getEndTime());
          // not vanished - can still be used for merging
        }
      }
    }
    return vanishedAnomalies;
  }

  private List<AnomalyDTO> retrieveRelevantAnomaliesFromDatabase(final Interval detectionInterval) {
    if (usage.equals(DetectionPipelineUsage.EVALUATION)) {
      return emptyList();
    } else if (usage.equals(DetectionPipelineUsage.DETECTION)) {
      final long mergeLowerBound = new DateTime(detectionInterval.getStart()).minus(mergeMaxGap)
          .minus(1)
          .getMillis();
      final long mergeUpperBound = new DateTime(detectionInterval.getEnd()).plus(mergeMaxGap)
          .plus(1)
          .getMillis();
      requireNonNull(alertId, "Cannot pull existing anomalies with null alertId.");
      Long enumerationItemId = null;
      if (enumerationItem != null) {
        enumerationItemId = requireNonNull(enumerationItem.getId(),
            "Enumeration item id is null. Cannot ensure enumeration item exists in "
                + "persistence layer before merging anomalies by enumeration.");
      }

      return anomalyManager.filter(new AnomalyFilter()
          .setAlertId(alertId)
          .setEnumerationItemId(enumerationItemId)
          .setStartEndWindow(new Interval(mergeLowerBound, mergeUpperBound))
      );
    } else {
      throw new UnsupportedOperationException("Unknown DetectionPipelineUsage: " + usage);
    }
  }

  @VisibleForTesting
  protected List<AnomalyDTO> doMerge(final List<AnomalyDTO> operatorAnomalies,
      final List<AnomalyDTO> persistenceAnomalies) {
    // use a set that maintains order
    final List<AnomalyDTO> sortedAnomalies = combineAndSort(operatorAnomalies,
        persistenceAnomalies);
    final Set<AnomalyDTO> anomaliesToUpdate = new LinkedHashSet<>();
    // two parents are maintained: one for normal anomalies and one for anomalies to ignore
    // the merge happens independently for these 2 kinds of anomalies
    // this is done in the same loop to perform the replay logic first
    AnomalyDTO parentCandidate = null;
    AnomalyDTO ignoredParentCandidate = null;
    AnomalyDTO previousAnomaly = null;
    // sorted anomalies look like
    // [parentWithChild, child, replay, child, replay, parentWithNoChild, replay, replayNew]
    for (final AnomalyDTO anomaly : sortedAnomalies) {
      if (previousAnomaly != null && previousAnomaly.getId() != null && anomaly.getId() == null) {
        // apply replay checks
        if (startEndEquals(previousAnomaly, anomaly)) {
          if (currentValueHasChanged(previousAnomaly, anomaly)) {
            addReplayLabel(previousAnomaly, newOutdatedLabel());
            anomaliesToUpdate.add(previousAnomaly);
            addReplayLabel(anomaly, newAfterReplayLabel());
            if (previousAnomaly == parentCandidate) {
              // the current parentCandidate is ignored now - move it to ignoredParentCandidate
              parentCandidate = null;
              optional(ignoredParentCandidate).ifPresent(anomaliesToUpdate::add);
              ignoredParentCandidate = previousAnomaly;
            }
          } else {
            // if an anomaly is in ignore state and has never been notified, but is now changing to not ignore, then ensure it is notified 
            // by hack on the createTime to ensure the notification logic finds this anomaly
            // see spec decision table https://docs.google.com/document/d/1bSbv4XhTQsdGR1XVM_dYL1cK9Q6JlntvzNYmmMiXQRI/edit
            // FIXME CYRIL - something similar should also be done at the parent level 
            if (isIgnore(previousAnomaly) && !previousAnomaly.isNotified() && !isIgnore(anomaly)) {
              previousAnomaly.setCreateTime(new Timestamp(System.currentTimeMillis()));
            }
            // update the existing anomaly with minor changes - drop the new anomaly
            updateAnomalyWithNewValues(previousAnomaly, anomaly);
            // labels depend on the values - so pick the latest labels
            previousAnomaly.setAnomalyLabels(anomaly.getAnomalyLabels());
            anomaliesToUpdate.add(previousAnomaly);
            continue;
          }
        }
      }
      previousAnomaly = anomaly;

      // skip child anomalies. merge their parents instead
      if (anomaly.isChild()) {
        continue;
      }
      if (isIgnore(anomaly)) {
        // maybe ignored anomalies should never be merged?
        if (ignoredParentCandidate == null) {
          ignoredParentCandidate = anomaly;
          continue;
        }
        if (shouldMerge(ignoredParentCandidate, anomaly)) {
          // anomaly is merged into the existing parent
          mergeIntoParent(ignoredParentCandidate, anomaly);
        } else {
          // by properties of the sort the current parentCandidate will not merge anymore
          // put it in list of anomalies and make the current anomaly the new parentCandidate
          anomaliesToUpdate.add(ignoredParentCandidate);
          ignoredParentCandidate = anomaly;
        }
      } else {
        if (parentCandidate == null) {
          parentCandidate = anomaly;
          continue;
        }
        if (shouldMerge(parentCandidate, anomaly)) {
          // anomaly is merged into the existing parent
          mergeIntoParent(parentCandidate, anomaly);
        } else {
          // by properties of the sort the current parentCandidate will not merge anymore
          // put it in list of anomalies and make the current anomaly the new parentCandidate
          anomaliesToUpdate.add(parentCandidate);
          parentCandidate = anomaly;
        }
      }
    }
    // add last parent candidate
    // todo cyril - anomaliesToUpdate is sorted. maintaining order at insertion for those 2 would make things easier to understand when debugging and testing 
    //  need to make sure the operation is not expensive though - anomaliesToUpdate can be big 
    optional(parentCandidate).ifPresent(anomaliesToUpdate::add);
    optional(ignoredParentCandidate).ifPresent(anomaliesToUpdate::add);

    return new ArrayList<>(anomaliesToUpdate);
  }

  private boolean currentValueHasChanged(final AnomalyDTO existingA, final AnomalyDTO newA) {
    final double existingCurrentVal = existingA.getAvgCurrentVal();
    final double newCurrentVal = newA.getAvgCurrentVal();
    final boolean percentageHasChanged = reNotifyPercentageThreshold >= 0
        && ((existingCurrentVal == 0 && newCurrentVal != 0)
        || computeValueChangePercentage(existingCurrentVal, newCurrentVal)
        > reNotifyPercentageThreshold);

    final boolean absoluteHasChanged = reNotifyAbsoluteThreshold >= 0
        && Math.abs(existingCurrentVal - newCurrentVal) > reNotifyAbsoluteThreshold;

    return percentageHasChanged && absoluteHasChanged;
  }

  /**
   * Merge anomalies if
   * - parent exists
   * - parent end time and child start time respects max allowed gap between anomalies
   * - parent anomaly post merge respects maxDuration
   *
   * @param parent parent anomaly
   * @param child child anomaly
   * @return whether they should be merged
   */
  private boolean shouldMerge(final AnomalyDTO parent, final AnomalyDTO child) {
    requireNonNull(parent);
    requireNonNull(child);

    final boolean parentIsIgnore = isIgnore(parent);
    final boolean childIsIgnore = isIgnore(child);
    checkState(parentIsIgnore == childIsIgnore,
        "Implementation error. Parent and child should have the same value for "
            + "isIgnore. Please reach out to support. Parent: %s. Child: %s", parent, child);

    final String parentPatternKey = patternKey(parent);
    final String childPatternKey = patternKey(child);
    if (!parentPatternKey.equals(childPatternKey)) {
      // never merge anomalies that don't go in the same direction
      return false;
    }

    final DateTime childStartTime = new DateTime(child.getStartTime(), chronology);
    final DateTime childEndTime = new DateTime(child.getEndTime(), chronology);

    return childStartTime.minus(mergeMaxGap).isBefore(parent.getEndTime())
        && (child.getEndTime() <= parent.getEndTime()
        || childEndTime.minus(mergeMaxDuration).isBefore(parent.getStartTime()));
  }

  @VisibleForTesting
  protected void setChronology(final Chronology chronology) {
    this.chronology = chronology;
  }

  public static class Factory implements AnomalyPostProcessorFactory {

    @Override
    public String name() {
      return NAME;
    }

    @Override
    public AnomalyPostProcessor build(final Map<String, Object> params,
        final PostProcessingContext context) {
      final AnomalyMergerPostProcessorSpec spec = VANILLA_OBJECT_MAPPER.convertValue(params,
          AnomalyMergerPostProcessorSpec.class);
      spec.setAnomalyManager(context.getMergedAnomalyResultManager());
      spec.setAlertId(context.getAlertId());
      spec.setUsage(context.getUsage());
      spec.setEnumerationItemDTO(context.getEnumerationItemDTO());

      return new AnomalyMergerPostProcessor(spec);
    }
  }
}
