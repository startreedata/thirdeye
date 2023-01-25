/*
 * Copyright 2023 StarTree Inc
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

import static ai.startree.thirdeye.spi.util.AnomalyUtils.isIgnore;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.PostProcessingContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
 * from different enumerations. Merge after dx can be implemented by adding enumerationItem to the
 * anomaly key.
 */
public class AnomalyMergerPostProcessor implements AnomalyPostProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyMergerPostProcessor.class);
  private static final String NAME = "ANOMALY_MERGER";
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

    // more children
    return -1 * Integer.compare(o1.getChildren().size(), o2.getChildren().size());
  };
  @VisibleForTesting
  protected static final Period DEFAULT_MERGE_MAX_GAP = Period.hours(2);
  @VisibleForTesting
  protected static final Period DEFAULT_ANOMALY_MAX_DURATION = Period.days(7);

  private final Period mergeMaxGap;
  private final Period mergeMaxDuration;
  private final @Nullable Long alertId;
  private final @Nullable EnumerationItemDTO enumerationItem;
  private final DetectionPipelineUsage usage;
  private final AnomalyManager anomalyManager;

  // obtained at runtime
  private Chronology chronology;

  public AnomalyMergerPostProcessor(final AnomalyMergerPostProcessorSpec spec) {
    this.mergeMaxGap = isoPeriod(spec.getMergeMaxGap(), DEFAULT_MERGE_MAX_GAP);
    this.mergeMaxDuration = isoPeriod(spec.getMergeMaxDuration(), DEFAULT_ANOMALY_MAX_DURATION);
    this.alertId = spec.getAlertId();
    this.usage = spec.getUsage();
    this.enumerationItem = spec.getEnumerationItemDTO();

    this.anomalyManager = spec.getMergedAnomalyResultManager();
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {
    if (mergeMaxGap.equals(Period.ZERO)) {
      // short-circuit - merging is disabled
      return resultMap;
    }

    chronology = detectionInterval.getChronology();
    for (final OperatorResult operatorResult : resultMap.values()) {
      postProcessResult(operatorResult);
    }

    return resultMap;
  }

  private void postProcessResult(final OperatorResult operatorResult) {
    final List<AnomalyDTO> operatorAnomalies = operatorResult.getAnomalies();
    if (operatorAnomalies == null) {
      return;
    }

    final List<AnomalyDTO> mergedAnomalies = merge(operatorAnomalies);
    // hack - operatorResult is not mutable - exploit list mutability to update the anomalies
    //  could silently bug if a protective copy is returned by an OperatorResult implementation
    // ensure no protective copy is done comparing references
    checkArgument(operatorAnomalies == operatorResult.getAnomalies());
    operatorAnomalies.clear();
    operatorAnomalies.addAll(mergedAnomalies);
  }

  protected List<AnomalyDTO> merge(
      final List<AnomalyDTO> operatorAnomalies) {
    if (operatorAnomalies.isEmpty()) {
      return emptyList();
    }
    final List<AnomalyDTO> persistenceAnomalies = retrieveRelevantAnomaliesFromDatabase(
        operatorAnomalies);
    final List<AnomalyDTO> allAnomalies = combineAndSort(operatorAnomalies,
        persistenceAnomalies);

    return doMerge(allAnomalies);
  }

  private List<AnomalyDTO> retrieveRelevantAnomaliesFromDatabase(
      final List<AnomalyDTO> anomalies) {
    if (usage.equals(DetectionPipelineUsage.EVALUATION)) {
      return emptyList();
    } else if (usage.equals(DetectionPipelineUsage.DETECTION)) {
      final long minTime = anomalies.stream()
          .map(AnomalyDTO::getStartTime)
          .mapToLong(e -> e)
          .min()
          .orElseThrow(() -> new RuntimeException(String.format(
              "When trying to merge anomalies for alert id %s: No startTime in the anomalies.",
              alertId)));

      final long maxTime = anomalies.stream()
          .map(AnomalyDTO::getEndTime)
          .mapToLong(e -> e)
          .max()
          .orElseThrow(() -> new RuntimeException(String.format(
              "When trying to merge anomalies for alert id %s: No endTime in the anomalies.",
              alertId)));

      final long mergeLowerBound = new DateTime(minTime, chronology).minus(mergeMaxGap)
          .minus(1)
          .getMillis();
      final long mergeUpperBound = new DateTime(maxTime, chronology).plus(mergeMaxGap)
          .plus(1)
          .getMillis();
      requireNonNull(alertId, "Cannot pull existing anomalies with null alertId.");
      Long enumerationItemId = null;
      if (enumerationItem != null) {
        enumerationItemId = requireNonNull(enumerationItem.getId(),
            "Enumeration item id is null. Cannot ensure enumeration item exists in persistence layer before merging anomalies by enumeration.");
      }
      return anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(
          mergeLowerBound,
          mergeUpperBound,
          alertId,
          enumerationItemId);
    } else {
      throw new UnsupportedOperationException("Unknown DetectionPipelineUsage: " + usage);
    }
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

  /**
   * Expect anomalies to be sorted with {@link #COMPARATOR}.
   */
  @VisibleForTesting
  protected List<AnomalyDTO> doMerge(
      final Collection<AnomalyDTO> sortedAnomalies) {
    final List<AnomalyDTO> anomaliesToUpdate = new ArrayList<>();
    AnomalyDTO parentCandidate = null;
    for (final AnomalyDTO anomaly : sortedAnomalies) {
      // skip child anomalies. merge their parents instead
      if (anomaly.isChild()) {
        continue;
      }
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
    // add last parent candidate
    anomaliesToUpdate.add(parentCandidate);

    return anomaliesToUpdate;
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
  private boolean shouldMerge(final AnomalyDTO parent,
      final AnomalyDTO child) {
    requireNonNull(parent);
    requireNonNull(child);

    final boolean parentIsIgnore = isIgnore(parent);
    final boolean childIsIgnore = isIgnore(child);
    if (parentIsIgnore != childIsIgnore) {
      // never merge anomalies with different ignore value
      return false;
    }

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

  private static String patternKey(final AnomalyDTO anomaly) {
    String patternKey = "";
    if (!Double.isNaN(anomaly.getAvgBaselineVal()) && !Double.isNaN(anomaly.getAvgCurrentVal())) {
      patternKey = (anomaly.getAvgCurrentVal() > anomaly.getAvgBaselineVal()) ? "UP" : "DOWN";
    }
    return patternKey;
  }

  private void mergeIntoParent(final AnomalyDTO parent,
      final AnomalyDTO child) {
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
    final List<AnomalyLabelDTO> mergedAnomalyLabels = mergeAnomalyLabels(parent.getAnomalyLabels(),
        child.getAnomalyLabels());
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
    to.setStartTime(from.getStartTime());
    to.setEndTime(from.getEndTime());
    to.setMetric(from.getMetric());
    to.setCollection(from.getCollection());
    to.setDetectionConfigId(from.getDetectionConfigId());
    to.setAnomalyResultSource(from.getAnomalyResultSource());
    to.setAvgBaselineVal(from.getAvgBaselineVal());
    to.setAvgCurrentVal(from.getAvgCurrentVal());
    to.setFeedback(from.getFeedback());
    to.setAnomalyFeedbackId(from.getAnomalyFeedbackId());
    to.setScore(from.getScore());
    to.setWeight(from.getWeight());
    to.setProperties(from.getProperties());
    to.setSeverityLabel(from.getSeverityLabel());
    // FIXME CYRIL BEFORE MERGE NOT SURE IF THIS WORKS FINE ANYMORE
    optional(from.getEnumerationItem())
        .map(AnomalyMergerPostProcessor::cloneEnumerationRef)
        .ifPresent(to::setEnumerationItem);
    return to;
  }

  /**
   * Merge child's properties into parent's properties.
   * If the property exists in both then use parent's property.
   * For property = "detectorComponentName", combine the parent and child.
   *
   * @param parent The parent anomaly's properties.
   * @param child The child anomaly's properties.
   */
  private void mergeAnomalyProperties(final Map<String, String> parent,
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

  private @Nullable List<AnomalyLabelDTO> mergeAnomalyLabels(
      final @Nullable List<AnomalyLabelDTO> parentLabels,
      @Nullable final List<AnomalyLabelDTO> childLabels) {
    if (parentLabels == null && childLabels == null) {
      return null;
    } else if (parentLabels == null) {
      return childLabels;
    } else if (childLabels == null) {
      return parentLabels;
    }

    // simple merging logic based on hash - can be enhanced later
    final Set<AnomalyLabelDTO> labels = new HashSet<>(childLabels);
    labels.addAll(parentLabels);

    return new ArrayList<>(labels);
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
      final AnomalyMergerPostProcessorSpec spec = new ObjectMapper().convertValue(params,
          AnomalyMergerPostProcessorSpec.class);
      spec.setMergedAnomalyResultManager(context.getMergedAnomalyResultManager());
      spec.setAlertId(context.getAlertId());
      spec.setUsage(context.getUsage());
      spec.setEnumerationItemDTO(context.getEnumerationItemDTO());

      return new AnomalyMergerPostProcessor(spec);
    }
  }
}
