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
package ai.startree.thirdeye.worker.task.runner;

import static ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator.getDateTimeZone;
import static ai.startree.thirdeye.spi.Constants.GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_ALERT_PIPELINE_EXECUTION;
import static ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO.TIME_SERIES_SNAPSHOT_KEY;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.notification.AnomalyTimelinesView;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnomalyMerger {

  @VisibleForTesting
  protected static final Period DEFAULT_MERGE_MAX_GAP = Period.hours(2);
  @VisibleForTesting
  protected static final Period DEFAULT_ANOMALY_MAX_DURATION = Period.days(7);
  private static final Logger LOG = LoggerFactory.getLogger(AnomalyMerger.class);
  private static final String PROP_PATTERN_KEY = "pattern";
  private static final String PROP_GROUP_KEY = "groupKey";
  private static final Interval DUMMY_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);
  private static final Comparator<MergedAnomalyResultDTO> COMPARATOR = (o1, o2) -> {
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
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public AnomalyMerger(final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertTemplateRenderer alertTemplateRenderer) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertTemplateRenderer = alertTemplateRenderer;
  }

  public static MergedAnomalyResultDTO copyAnomalyInfo(final MergedAnomalyResultDTO from,
      final MergedAnomalyResultDTO to) {
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
    to.setType(from.getType());
    to.setSeverityLabel(from.getSeverityLabel());
    optional(from.getEnumerationItem())
        .map(AnomalyMerger::cloneEnumerationRef)
        .ifPresent(to::setEnumerationItem);
    return to;
  }

  private static EnumerationItemDTO cloneEnumerationRef(final EnumerationItemDTO ei) {
    final var clone = new EnumerationItemDTO();
    clone.setId(requireNonNull(ei.getId(), "enumeration item id is null"));
    return clone;
  }

  /**
   * Combine two components with comma separated.
   * For example, will combine "component1" and "component2" into "component1, component2".
   *
   * @param component1 The first component.
   * @param component2 The second component.
   * @return The combined components.
   */
  private static String combineComponents(final String component1, final String component2) {
    final List<String> components = new ArrayList<>();
    components.addAll(Arrays.asList(component1.split(
        Constants.PROP_DETECTOR_COMPONENT_NAME_DELIMETER)));
    components.addAll(Arrays.asList(component2.split(
        Constants.PROP_DETECTOR_COMPONENT_NAME_DELIMETER)));
    return components.stream().distinct().collect(Collectors.joining(
        Constants.PROP_DETECTOR_COMPONENT_NAME_DELIMETER));
  }

  /**
   * A helper function to merge time series snapshot of two anomalies. This function assumes that
   * the time series of
   * both parent and child anomalies are aligned with the metric granularity boundary.
   *
   * @param parent time series snapshot of parent anomaly
   * @param child time series snapshot of parent anaomaly
   * @return merged time series snapshot based on timestamps
   */
  private static AnomalyTimelinesView mergeTimeSeriesSnapshot(final AnomalyTimelinesView parent,
      final AnomalyTimelinesView child) {
    final AnomalyTimelinesView mergedTimeSeriesSnapshot = new AnomalyTimelinesView();
    int i = 0;
    int j = 0;
    while (i < parent.getTimeBuckets().size() && j < child.getTimeBuckets().size()) {
      final long parentTime = parent.getTimeBuckets().get(i).getCurrentStart();
      final long childTime = child.getTimeBuckets().get(j).getCurrentStart();
      if (parentTime == childTime) {
        // use the values in parent anomalies when the time series overlap
        mergedTimeSeriesSnapshot.addTimeBuckets(parent.getTimeBuckets().get(i));
        mergedTimeSeriesSnapshot.addCurrentValues(parent.getCurrentValues().get(i));
        mergedTimeSeriesSnapshot.addBaselineValues(parent.getBaselineValues().get(i));
        i++;
        j++;
      } else if (parentTime < childTime) {
        mergedTimeSeriesSnapshot.addTimeBuckets(parent.getTimeBuckets().get(i));
        mergedTimeSeriesSnapshot.addCurrentValues(parent.getCurrentValues().get(i));
        mergedTimeSeriesSnapshot.addBaselineValues(parent.getBaselineValues().get(i));
        i++;
      } else {
        mergedTimeSeriesSnapshot.addTimeBuckets(child.getTimeBuckets().get(j));
        mergedTimeSeriesSnapshot.addCurrentValues(child.getCurrentValues().get(j));
        mergedTimeSeriesSnapshot.addBaselineValues(child.getBaselineValues().get(j));
        j++;
      }
    }
    while (i < parent.getTimeBuckets().size()) {
      mergedTimeSeriesSnapshot.addTimeBuckets(parent.getTimeBuckets().get(i));
      mergedTimeSeriesSnapshot.addCurrentValues(parent.getCurrentValues().get(i));
      mergedTimeSeriesSnapshot.addBaselineValues(parent.getBaselineValues().get(i));
      i++;
    }
    while (j < child.getTimeBuckets().size()) {
      mergedTimeSeriesSnapshot.addTimeBuckets(child.getTimeBuckets().get(j));
      mergedTimeSeriesSnapshot.addCurrentValues(child.getCurrentValues().get(j));
      mergedTimeSeriesSnapshot.addBaselineValues(child.getBaselineValues().get(j));
      j++;
    }
    mergedTimeSeriesSnapshot.getSummary().putAll(parent.getSummary());
    for (final String key : child.getSummary().keySet()) {
      if (!mergedTimeSeriesSnapshot.getSummary().containsKey(key)) {
        mergedTimeSeriesSnapshot.getSummary().put(key, child.getSummary().get(key));
      }
    }
    return mergedTimeSeriesSnapshot;
  }

  public void mergeAndSave(final AlertDTO alert, final List<MergedAnomalyResultDTO> anomalies) {
    if (anomalies.isEmpty()) {
      return;
    }
    final AlertTemplateDTO templateWithProperties;
    try {
      templateWithProperties = alertTemplateRenderer.renderAlert(alert, DUMMY_INTERVAL);
    } catch (final ClassNotFoundException | IOException e) {
      throw new ThirdEyeException(ERR_ALERT_PIPELINE_EXECUTION, e.getCause());
    }

    final DateTimeZone dateTimeZone = getDateTimezone(templateWithProperties);
    final Period maxGap = getMaxGap(templateWithProperties);
    final long alertId = requireNonNull(alert.getId(),
        "Alert must be an existing alert for merging.");

    /* Handling normal anomalies with no enumeration item */
    final List<MergedAnomalyResultDTO> anomaliesWithNoEnumerationItem = anomalies.stream()
        .filter(a -> a.getEnumerationItem() == null)
        .collect(Collectors.toList());
    final List<MergedAnomalyResultDTO> mergedAnomalies = mergeAnomalies(
        anomaliesWithNoEnumerationItem,
        templateWithProperties,
        dateTimeZone,
        maxGap,
        alertId,
        null);

    /* Handling anomalies with enumeration item. Merging needs to happen per enumeration Item */
    final var mergedAnomaliesWithEnumerationItem = groupByEnumerationItem(anomalies)
        .entrySet()
        .stream()
        .flatMap(e -> mergeAnomalies(
            e.getValue(),
            templateWithProperties,
            dateTimeZone,
            maxGap,
            alertId,
            e.getKey()).stream())
        .collect(Collectors.toList());

    final List<MergedAnomalyResultDTO> allMergedAnomalies = new ArrayList<>(mergedAnomalies);
    allMergedAnomalies.addAll(mergedAnomaliesWithEnumerationItem);

    for (final MergedAnomalyResultDTO mergedAnomalyResultDTO : allMergedAnomalies) {
      final Long id = mergedAnomalyResultManager.save(mergedAnomalyResultDTO);
      if (id == null) {
        LOG.error("Failed to store anomaly: {}", mergedAnomalyResultDTO);
      }
    }
  }

  private Map<Long, List<MergedAnomalyResultDTO>> groupByEnumerationItem(
      final List<MergedAnomalyResultDTO> anomalies) {
    final Map<Long, List<MergedAnomalyResultDTO>> map = new HashMap<>();
    for (final MergedAnomalyResultDTO anomaly : anomalies) {
      final EnumerationItemDTO enumerationItem = anomaly.getEnumerationItem();
      if (enumerationItem != null) {
        final List<MergedAnomalyResultDTO> list = map.computeIfAbsent(
            enumerationItem.getId(),
            id -> new ArrayList<>()); // doesn't create a list everytime
        list.add(anomaly);
      }
    }
    return map;
  }

  private List<MergedAnomalyResultDTO> mergeAnomalies(
      final List<MergedAnomalyResultDTO> anomalies,
      final AlertTemplateDTO templateWithProperties,
      final DateTimeZone dateTimeZone,
      final Period maxGap,
      final long alertId,
      final Long enumerationItemId) {
    if (anomalies.isEmpty()) {
      return emptyList();
    }

    final List<MergedAnomalyResultDTO> existingAnomalies = retrieveRelevantAnomaliesFromDatabase(
        alertId,
        anomalies,
        maxGap,
        dateTimeZone,
        enumerationItemId);

    final List<MergedAnomalyResultDTO> sortedRelevantAnomalies = combineAndSort(anomalies,
        existingAnomalies);

    final Period maxDurationMillis = getMaxDuration(templateWithProperties);
    return merge(sortedRelevantAnomalies,
        maxGap,
        maxDurationMillis,
        dateTimeZone);
  }

  @VisibleForTesting
  List<MergedAnomalyResultDTO> combineAndSort(final List<MergedAnomalyResultDTO> anomalies,
      final List<MergedAnomalyResultDTO> existingAnomalies) {
    final List<MergedAnomalyResultDTO> generatedAndExistingAnomalies = new ArrayList<>();
    generatedAndExistingAnomalies.addAll(anomalies);
    generatedAndExistingAnomalies.addAll(existingAnomalies);

    // prepare a sorted list for processing
    generatedAndExistingAnomalies.sort(COMPARATOR);
    return generatedAndExistingAnomalies;
  }

  /**
   * Merge a list of anomalies given a max gap and a max duration.
   */
  @VisibleForTesting
  protected List<MergedAnomalyResultDTO> merge(final Collection<MergedAnomalyResultDTO> anomalies,
      final Period maxGap, final Period maxDurationMillis, final DateTimeZone dateTimeZone) {
    final List<MergedAnomalyResultDTO> anomaliesToUpdate = new ArrayList<>();
    final Map<AnomalyKey, MergedAnomalyResultDTO> parents = new HashMap<>();
    for (final MergedAnomalyResultDTO anomaly : anomalies) {
      // skip child anomalies. merge their parents instead
      if (anomaly.isChild()) {
        continue;
      }
      // Prevent merging of grouped anomalies - custom hashmap key
      final AnomalyKey key = createAnomalyKey(anomaly);
      final MergedAnomalyResultDTO parent = parents.get(key);
      if (parent == null) {
        parents.put(key, anomaly);
        continue;
      }
      if (shouldMerge(parent, anomaly, maxGap, maxDurationMillis, dateTimeZone)) {
        // anomaly is merged into the existing parent
        mergeIntoParent(parent, anomaly);
      } else {
        parents.put(key, anomaly);
        // previous parent may be overridden in map - make sure it is saved
        anomaliesToUpdate.add(parent);
      }
    }
    // save all parents
    anomaliesToUpdate.addAll(parents.values());

    return anomaliesToUpdate;
  }

  private AnomalyKey createAnomalyKey(final MergedAnomalyResultDTO anomaly) {
    final String groupKey = anomaly.getProperties().getOrDefault(PROP_GROUP_KEY, "");
    final String patternKey = getPatternKey(anomaly);
    return new AnomalyKey(anomaly.getMetric(),
        anomaly.getCollection(),
        anomaly.getDimensions(),
        StringUtils.join(Arrays.asList(groupKey, patternKey), ","),
        "",
        anomaly.getType());
  }

  private void mergeIntoParent(final MergedAnomalyResultDTO parent,
      final MergedAnomalyResultDTO child) {
    // fully merge into existing
    final Set<MergedAnomalyResultDTO> children = parent.getChildren();

    // if this parent has no children, then add a copy of itself as a child
    if (children.isEmpty()) {
      children.add(copyAnomalyInfo(parent, new MergedAnomalyResultDTO()));
    }
    // Extend the end time to match the child anomaly end time.
    parent.setEndTime(Math.max(parent.getEndTime(), child.getEndTime()));

    // merge the anomaly's properties into parent
    mergeAnomalyProperties(parent.getProperties(), child.getProperties());

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
  private boolean shouldMerge(final MergedAnomalyResultDTO parent,
      final MergedAnomalyResultDTO child,
      final Period maxGap,
      final Period maxDuration,
      final DateTimeZone dateTimeZone) {
    requireNonNull(parent);

    final DateTime childStartTime = new DateTime(child.getStartTime(), dateTimeZone);
    final DateTime childEndTime = new DateTime(child.getEndTime(), dateTimeZone);

    return childStartTime.minus(maxGap).isBefore(parent.getEndTime())
        && (child.getEndTime() <= parent.getEndTime()
        || childEndTime.minus(maxDuration).isBefore(parent.getStartTime()));
  }

  private String getPatternKey(final MergedAnomalyResultDTO anomaly) {
    String patternKey = "";
    if (anomaly.getProperties().containsKey(PROP_PATTERN_KEY)) {
      patternKey = anomaly.getProperties().get(PROP_PATTERN_KEY);
    } else if (!Double.isNaN(anomaly.getAvgBaselineVal())
        && !Double.isNaN(anomaly.getAvgCurrentVal())) {
      patternKey = (anomaly.getAvgCurrentVal() > anomaly.getAvgBaselineVal()) ? "UP" : "DOWN";
    }
    return patternKey;
  }

  protected Period getMaxGap(final AlertTemplateDTO templateWithProperties) {
    return optional(templateWithProperties.getMetadata())
        .map(AlertMetadataDTO::getMergeMaxGap)
        // templates can have an empty string as default property
        .filter(StringUtils::isNotEmpty)
        .map(TimeUtils::isoPeriod)
        .orElse(DEFAULT_MERGE_MAX_GAP);
  }

  private Period getMaxDuration(final AlertTemplateDTO templateWithProperties) {
    return optional(templateWithProperties.getMetadata())
        .map(AlertMetadataDTO::getMergeMaxDuration)
        // templates can have an empty string as default property
        .filter(StringUtils::isNotEmpty)
        .map(TimeUtils::isoPeriod)
        .orElse(DEFAULT_ANOMALY_MAX_DURATION);
  }

  private DateTimeZone getDateTimezone(final AlertTemplateDTO templateWithProperties) {
    return optional(getDateTimeZone(templateWithProperties))
        .orElse(Constants.DEFAULT_TIMEZONE);
  }

  private List<MergedAnomalyResultDTO> retrieveRelevantAnomaliesFromDatabase(final long alertId,
      final List<MergedAnomalyResultDTO> anomalies,
      final Period maxGap,
      final DateTimeZone dateTimeZone,
      final Long enumerationItemId) {
    final long minTime = anomalies.stream()
        .map(MergedAnomalyResultDTO::getStartTime)
        .mapToLong(e -> e)
        .min()
        .orElseThrow(() -> new RuntimeException(String.format(
            "When trying to merge anomalies for alert id %s: No startTime in the anomalies.",
            alertId)));

    final long maxTime = anomalies.stream()
        .map(MergedAnomalyResultDTO::getEndTime)
        .mapToLong(e -> e)
        .max()
        .orElseThrow(() -> new RuntimeException(String.format(
            "When trying to merge anomalies for alert id %s: No endTime in the anomalies.",
            alertId)));

    final long mergeLowerBound = new DateTime(minTime, dateTimeZone).minus(maxGap)
        .minus(1)
        .getMillis();
    final long mergeUpperBound = new DateTime(maxTime, dateTimeZone).plus(maxGap)
        .plus(1)
        .getMillis();

    return mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(mergeLowerBound,
        mergeUpperBound,
        alertId,
        enumerationItemId);
  }

  /**
   * Merge child's properties into parent's properties.
   * If the property exists in both then use parent's property.
   * For property = "detectorComponentName", combine the parent and child.
   *
   * @param parent The parent anomaly's properties.
   * @param child The child anomaly's properties.
   */
  private void mergeAnomalyProperties(final Map<String, String> parent, final Map<String, String> child) {
    for (final String key : child.keySet()) {
      if (!parent.containsKey(key)) {
        parent.put(key, child.get(key));
      } else {
        // combine detectorComponentName
        if (key.equals(GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME)) {
          final String component = combineComponents(parent.get(
              GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME), child.get(
              GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME));
          parent.put(GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME, component);
        }
        // combine time series snapshot of parent and child anomalies
        if (key.equals(MergedAnomalyResultDTO.TIME_SERIES_SNAPSHOT_KEY)) {
          try {
            final AnomalyTimelinesView parentTimeSeries = AnomalyTimelinesView
                .fromJsonString(parent.get(TIME_SERIES_SNAPSHOT_KEY));
            final AnomalyTimelinesView childTimeSeries = AnomalyTimelinesView
                .fromJsonString(child.get(TIME_SERIES_SNAPSHOT_KEY));
            parent.put(TIME_SERIES_SNAPSHOT_KEY,
                mergeTimeSeriesSnapshot(parentTimeSeries, childTimeSeries).toJsonString());
          } catch (final Exception e) {
            LOG.warn("Unable to merge time series, so skipping...", e);
          }
        }
      }
    }
  }
}
