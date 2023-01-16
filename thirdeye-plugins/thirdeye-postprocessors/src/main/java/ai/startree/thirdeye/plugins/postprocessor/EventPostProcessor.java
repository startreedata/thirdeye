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

import static ai.startree.thirdeye.spi.Constants.COL_EVENT_END;
import static ai.startree.thirdeye.spi.Constants.COL_EVENT_NAME;
import static ai.startree.thirdeye.spi.Constants.COL_EVENT_START;
import static ai.startree.thirdeye.spi.detection.AnomalyDetector.KEY_CURRENT_EVENTS;
import static ai.startree.thirdeye.spi.util.AnomalyUtils.addLabel;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datastructures.Interval1D;
import ai.startree.thirdeye.datastructures.IntervalSearchTree;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.PostProcessingContext;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

public class EventPostProcessor implements AnomalyPostProcessor {

  private static final boolean DEFAULT_IGNORE = false;
  private static final Period DEFAULT_BEFORE_MARGIN = Period.ZERO;
  private static final Period DEFAULT_AFTER_MARGIN = Period.ZERO;
  private static final String NAME = "EVENTS";

  private final boolean ignore;
  private final Period beforeMargin;
  private final Period afterMargin;

  public EventPostProcessor(final EventPostProcessorSpec spec) {
    this.ignore = optional(spec.getIgnore()).orElse(DEFAULT_IGNORE);
    this.beforeMargin = isoPeriod(spec.getBeforeEventMargin(), DEFAULT_BEFORE_MARGIN);
    this.afterMargin = isoPeriod(spec.getAfterEventMargin(), DEFAULT_AFTER_MARGIN);
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {

    final OperatorResult eventsInput = resultMap.remove(KEY_CURRENT_EVENTS);
    if (eventsInput == null) {
      return resultMap;
    }
    checkArgument(eventsInput instanceof DataTable);
    final DataFrame eventsDf = ((DataTable) eventsInput).getDataFrame();
    if (eventsDf.isEmpty()) {
      return resultMap;
    }

    final Chronology chronology = detectionInterval.getChronology();
    final IntervalSearchTree<String> eventsSearchTree = buildIntervalSearchTree(eventsDf,
        chronology);

    for (final OperatorResult operatorResult : resultMap.values()) {
      postProcessResult(operatorResult, eventsSearchTree);
    }

    return resultMap;
  }

  private void postProcessResult(final OperatorResult operatorResult,
      final IntervalSearchTree<String> eventsSearchTree) {
    final List<MergedAnomalyResultDTO> anomalies = operatorResult.getAnomalies();
    if (anomalies == null) {
      return;
    }

    for (final MergedAnomalyResultDTO anomalyResultDTO : anomalies) {
      final Interval1D searchInterval = Interval1D.of(anomalyResultDTO.getStartTime(),
          anomalyResultDTO.getEndTime());
      final Entry<Interval1D, Set<String>> match = eventsSearchTree.search(searchInterval);
      if (match != null) {
        final Set<String> eventNames = match.getValue();
        final String labelName = labelName(eventNames);
        final AnomalyLabelDTO newLabel = new AnomalyLabelDTO().setIgnore(ignore)
            .setName(labelName);
        addLabel(anomalyResultDTO, newLabel);
      }
    }
  }

  private String labelName(final Set<String> eventNames) {
    final String eventList = String.join(",", eventNames);
    final String plural = eventNames.size() == 1 ? "" : "s";
    return "Anomaly happens during " + eventList + " event" + plural;
  }

  /**
   * Expects an event dataframe with columns
   * {@value ai.startree.thirdeye.spi.Constants#COL_EVENT_NAME},
   * {@value ai.startree.thirdeye.spi.Constants#COL_EVENT_START},
   * {@value ai.startree.thirdeye.spi.Constants#COL_EVENT_END}.
   */
  private IntervalSearchTree<String> buildIntervalSearchTree(final DataFrame eventsDf,
      final Chronology chronology) {
    final IntervalSearchTree<String> searchTree = new IntervalSearchTree<>();
    for (int i = 0; i < eventsDf.size(); i++) {
      final DateTime startWithMargin = new DateTime(eventsDf.getLong(COL_EVENT_START, i),
          chronology).minus(beforeMargin);
      final DateTime endWithMargin = new DateTime(eventsDf.getLong(COL_EVENT_END, i),
          chronology).plus(afterMargin);
      searchTree.put(Interval1D.of(startWithMargin.getMillis(), endWithMargin.getMillis()),
          eventsDf.getString(COL_EVENT_NAME, i));
    }

    return searchTree;
  }

  public static class Factory implements AnomalyPostProcessorFactory {

    @Override
    public String name() {
      return NAME;
    }

    @Override
    public AnomalyPostProcessor build(final Map<String, Object> params, final PostProcessingContext context) {
      final EventPostProcessorSpec spec = new ObjectMapper()
          .convertValue(params, EventPostProcessorSpec.class);
      return new EventPostProcessor(spec);
    }
  }
}
