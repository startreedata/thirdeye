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
package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.StringUtils.levenshteinDistance;
import static ai.startree.thirdeye.util.StringUtils.timeFormatterFor;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.rca.RcaInfo;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.rootcause.events.IntervalSimilarityScoring;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.api.RelatedAnomaliesAnalysisApi;
import ai.startree.thirdeye.spi.api.RelatedEventsAnalysisApi;
import ai.startree.thirdeye.spi.api.TextualAnalysis;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventContextDto;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;

@Singleton
public class RcaRelatedService {

  private static final int MAX_SELECTED_EVENTS_PER_TYPE_FOR_TEXT = 3;
  private static final int SAME_EVENT_LEVENSHTEIN_THRESHOLD = 2;
  private static final int MAX_EVENTS_FOR_TEXT = 6;

  private final RcaInfoFetcher rcaInfoFetcher;
  private final EventManager eventDAO;
  private final AnomalyManager anomalyDAO;
  private final AlertManager alertDAO;
  private final EnumerationItemManager enumerationItemDAO;
  private final AuthorizationManager authorizationManager;

  @Inject
  public RcaRelatedService(final RcaInfoFetcher rcaInfoFetcher, final EventManager eventDAO,
      final AnomalyManager anomalyDAO, final AlertManager alertDAO,
      final EnumerationItemManager enumerationItemDAO,
      final AuthorizationManager authorizationManager) {

    this.rcaInfoFetcher = rcaInfoFetcher;
    this.eventDAO = eventDAO;
    this.anomalyDAO = anomalyDAO;
    this.alertDAO = alertDAO;
    this.enumerationItemDAO = enumerationItemDAO;
    this.authorizationManager = authorizationManager;
  }

  @NonNull
  public List<EventApi> getRelatedEvents(final ThirdEyePrincipal principal, final Long anomalyId, final String type,
      final IntervalSimilarityScoring scoring, final int limit, final Period lookaround)
      throws IOException, ClassNotFoundException {
    final AnomalyDTO anomalyDto = ensureExists(anomalyDAO.findById(anomalyId),
        String.format("Anomaly ID: %d", anomalyId));
    authorizationManager.ensureCanRead(principal, anomalyDto);
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(anomalyDto);
    return getRelatedEvents(rcaInfo, type, scoring, limit, lookaround);
  }

  @NonNull
  private List<EventApi> getRelatedEvents(final RcaInfo rcaInfo,
      final @org.checkerframework.checker.nullness.qual.Nullable String type,
      final IntervalSimilarityScoring scoring, final int limit, final Period lookaround) {
    final Interval anomalyInterval = new Interval(rcaInfo.anomaly().getStartTime(),
        rcaInfo.anomaly().getEndTime(), rcaInfo.chronology());
    final long startWithLookback = anomalyInterval.getStart().minus(lookaround).getMillis();
    final long endWithLookahead = Math.max(anomalyInterval.getStart().plus(lookaround).getMillis(),
        anomalyInterval.getEnd().getMillis());

    final @NonNull EventContextDto eventContext = rcaInfo.eventContext();
    // todo cyril make the type parameter a list - ask FrontEnd if it's ok first
    final List<@NonNull String> types = optional(type).map(List::of)
        .orElse(optional(eventContext.getTypes()).map(Templatable::getValue).orElse(List.of()));
    final List<EventDTO> events = eventDAO.findEventsBetweenTimeRangeInNamespace(startWithLookback,
        endWithLookahead, types,
        // todo rca dimension filters can be set at call time?
        eventContext.getSqlFilter(),
        rcaInfo.alert().namespace());

    final Comparator<EventDTO> comparator = Comparator.comparingDouble(
        (ToDoubleFunction<EventDTO>) dto -> scoring.score(anomalyInterval,
            new Interval(dto.getStartTime(), dto.getEndTime(), anomalyInterval.getChronology()),
            lookaround)).reversed();
    events.sort(comparator);

    return events.stream()
        .limit(limit)
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  @NonNull
  public RelatedEventsAnalysisApi getEventsAnalysis(final ThirdEyePrincipal principal, final Long anomalyId, final String type,
      final IntervalSimilarityScoring scoring, final int limit, final Period lookaround)
      throws IOException, ClassNotFoundException {
    final AnomalyDTO anomalyDto = ensureExists(anomalyDAO.findById(anomalyId),
        String.format("Anomaly ID: %d", anomalyId));
    authorizationManager.ensureCanRead(principal, anomalyDto);
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(anomalyDto);
    final List<EventApi> events = getRelatedEvents(rcaInfo, type, scoring, limit, lookaround);
    final RelatedEventsAnalysisApi result = new RelatedEventsAnalysisApi();
    result.setEvents(events);
    final String analysisText = generateEventsAnalysisText(events, rcaInfo);
    result.setTextualAnalysis(new TextualAnalysis().setText(analysisText));

    return result;
  }

  private String generateEventsAnalysisText(final List<EventApi> events, final RcaInfo rcaInfo) {
    if (events.isEmpty()) {
      return "No events related to this anomaly were found.";
    }
    final StringBuilder text = new StringBuilder();
    text.append("Some events might have caused the anomaly.\n");

    final List<EventApi> selectedEvents = selectEventsForText(events);
    for (final EventApi event : selectedEvents) {
      text.append(generateAnalysisText(event, rcaInfo));
      text.append("\n");
    }

    return text.toString();
  }

  /**
   * Select relevant events for textual analysis.
   * Limit the number of events to 3 per type.
   *
   * naive fuzzy matching cleaning:
   * Filter events that have names with a small levenshtein distance. They are most likely the same
   * events.
   */
  @NonNull
  private static List<EventApi> selectEventsForText(final List<EventApi> events) {
    final Map<String, List<EventApi>> typeToEvents = new HashMap<>();
    final LinkedList<EventApi> selectedEvents = new LinkedList<>();
    for (final EventApi e : events) {
      final List<EventApi> typeEvents = typeToEvents.computeIfAbsent(e.getType(),
          k -> new ArrayList<>());
      if (typeEvents.size() >= MAX_SELECTED_EVENTS_PER_TYPE_FOR_TEXT) {
        continue;
      }
      final boolean isNew = typeEvents.stream()
          .map(el -> el.getName().toLowerCase(Constants.DEFAULT_LOCALE))
          // very naive fuzzy matching
          .filter(el -> levenshteinDistance(el, e.getName().toLowerCase(Constants.DEFAULT_LOCALE))
              < SAME_EVENT_LEVENSHTEIN_THRESHOLD)
          .findFirst()
          .isEmpty();
      if (isNew) {
        if (typeEvents.isEmpty()) {
          // add at the beginning to ensure all different types of events have a chance of appearing in the analysis
          selectedEvents.addFirst(e);
        } else {
          selectedEvents.addLast(e);
        }
        typeEvents.add(e);
      }
    }

    return selectedEvents.subList(0, Math.min(selectedEvents.size(), MAX_EVENTS_FOR_TEXT));
  }

  // TODO add weekend analysis wordings eg: "the previous week-end or the following week-end)
  private static String generateAnalysisText(final EventApi event, final RcaInfo rcaInfo) {
    final DateTimeFormatter timeFormatter = timeFormatterFor(rcaInfo.granularity(),
        rcaInfo.chronology());
    final DateTime eventStart = new DateTime(event.getStartTime(), rcaInfo.chronology());

    final String timeRelation;
    if (event.getEndTime() <= rcaInfo.anomaly().getStartTime()) {
      timeRelation = "occurred just before, on " + eventStart.toString(timeFormatter);
      // todo replace just before by "x days before ..." the event
    } else if (event.getStartTime() >= rcaInfo.anomaly().getEndTime()) {
      // event happens after the anomaly without overlap
      // todo replace soon after by a more precise wording
      timeRelation = "happens soon after, on " + eventStart.toString(timeFormatter);
    } else {
      // event happens around the same time
      // todo differentiate left overlap, exact match and right overlap
      timeRelation = "happened at the same time on " + eventStart.toString(timeFormatter);
    }

    final List<CharSequence> words = List.of("The", event.getType(), "event", event.getName(),
        timeRelation);
    return String.join(" ", words) + ".";
  }

  public List<AnomalyApi> getRelatedAnomalies(final ThirdEyePrincipal principal, final long anomalyId,
      final IntervalSimilarityScoring scoring, final int limit, final Period lookaround)
      throws IOException, ClassNotFoundException {
    final AnomalyDTO anomalyDto = ensureExists(anomalyDAO.findById(anomalyId),
        String.format("Anomaly ID: %d", anomalyId));
    authorizationManager.ensureCanRead(principal, anomalyDto);
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(anomalyDto);
    return getRelatedAnomalies(rcaInfo, scoring, limit, lookaround);
  }

  public RelatedAnomaliesAnalysisApi getAnomaliesAnalysis(final ThirdEyePrincipal principal,
      final long anomalyId,
      final IntervalSimilarityScoring scoring, final int limit, final Period lookaround)
      throws IOException, ClassNotFoundException {
    final AnomalyDTO anomalyDto = ensureExists(anomalyDAO.findById(anomalyId),
        String.format("Anomaly ID: %d", anomalyId));
    authorizationManager.ensureCanRead(principal, anomalyDto);
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(anomalyDto);
    final List<AnomalyApi> anomalies = getRelatedAnomalies(rcaInfo, scoring, limit, lookaround);

    final RelatedAnomaliesAnalysisApi result = new RelatedAnomaliesAnalysisApi();
    result.setAnomalies(anomalies);
    final String analysisText = generateAnomaliesAnalysisText(anomalies, rcaInfo);
    result.setTextualAnalysis(new TextualAnalysis().setText(analysisText));

    return result;
  }

  @NonNull
  private List<AnomalyApi> getRelatedAnomalies(final RcaInfo rcaInfo,
      final IntervalSimilarityScoring scoring, final int limit, final Period lookaround) {
    final Interval anomalyInterval = new Interval(rcaInfo.anomaly().getStartTime(),
        rcaInfo.anomaly().getEndTime(), rcaInfo.chronology());
    final long startWithLookback = anomalyInterval.getStart().minus(lookaround).getMillis();
    final long endWithLookahead = Math.max(anomalyInterval.getStart().plus(lookaround).getMillis(),
        anomalyInterval.getEnd().getMillis());
    final List<AnomalyDTO> anomalies = anomalyDAO.filterWithNamespace(new AnomalyFilter()
        .setStartEndWindow(new Interval(startWithLookback, endWithLookahead))
        .setIsChild(false), rcaInfo.alert().namespace()
    );

    final Comparator<AnomalyDTO> comparator = Comparator.comparingDouble(
        (ToDoubleFunction<AnomalyDTO>) dto -> scoring.score(anomalyInterval,
            new Interval(dto.getStartTime(), dto.getEndTime(), anomalyInterval.getChronology()),
            lookaround)).reversed();
    anomalies.sort(comparator);

    return anomalies.stream()
        .limit(limit)
        .filter(dto -> !dto.getId().equals(rcaInfo.anomaly().getId()))
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  private String generateAnomaliesAnalysisText(final List<AnomalyApi> anomalies,
      final RcaInfo rcaInfo) {
    List<AnomalyApi> anomaliesOfSameAlertDifferentEnumAndClose = new ArrayList<>();
    // same metric means same alert, same enum
    List<AnomalyApi> anomaliesOfSameMetric = new ArrayList<>();
    List<AnomalyApi> otherAnomalies = new ArrayList<>();
    for (final AnomalyApi relatedAnomaly : anomalies) {
      if (isOfSameAlertDifferentEnumAndClose(rcaInfo.anomaly(), relatedAnomaly)) {
        anomaliesOfSameAlertDifferentEnumAndClose.add(relatedAnomaly);
      } else if (isOfSameAlertSameEnum(rcaInfo.anomaly(), relatedAnomaly)) {
        anomaliesOfSameMetric.add(relatedAnomaly);
      } else {
        otherAnomalies.add(relatedAnomaly);
      }
    }
    final StringBuilder text = new StringBuilder();
    text.append(textForAnomaliesOfSameAlertDifferentEnum(anomaliesOfSameAlertDifferentEnumAndClose,
        rcaInfo));
    text.append(textForAnomaliesOfSameMetric(anomaliesOfSameMetric, rcaInfo));
    text.append(textForOtherAnomalies(otherAnomalies, rcaInfo));

    return text.toString();
  }

  private StringBuilder textForAnomaliesOfSameAlertDifferentEnum(
      final List<AnomalyApi> anomaliesOfSameAlertDifferentEnumAndClose, final RcaInfo rcaInfo) {
    final StringBuilder text = new StringBuilder();
    if (!anomaliesOfSameAlertDifferentEnumAndClose.isEmpty()) {
      // similar anomalies in other dimensions
      final List<String> relatedEnums = anomaliesOfSameAlertDifferentEnumAndClose.stream()
          .map(e -> e.getEnumerationItem().getId())
          .map(id -> optional(enumerationItemDAO.findById(id)).map(EnumerationItemDTO::getName)
              .orElse("enumerationItem " + id))
          .distinct()
          .collect(Collectors.toList());
      text.append(
          "Multiple dimensions may be affected by the same issue: anomalies were detected at the same time for ");
      text.append(String.join(", ", relatedEnums.subList(0, Math.min(3, relatedEnums.size()))));
      if (relatedEnums.size() > 3) {
        text.append(" and ").append(relatedEnums.size() - 3).append(" others");
      }
      text.append(". ");
    } else if (rcaInfo.anomaly().getEnumerationItem() != null) {
      // the problem is specific to this enumeration
      text.append(
          "The anomaly is specific to this dimension. No anomalies were detected in other dimensions of this alert. ");
    }

    return text;
  }

  private static StringBuilder textForAnomaliesOfSameMetric(
      final List<AnomalyApi> anomaliesOfSameAlertSameEnum, final RcaInfo rcaInfo) {
    final DateTimeFormatter timeFormatter = timeFormatterFor(rcaInfo.granularity(),
        rcaInfo.chronology());
    final StringBuilder text = new StringBuilder();
    if (anomaliesOfSameAlertSameEnum.isEmpty()) {
      return text;
    }
    // the metric is not stable, too sensitive
    text.append("The metric has other anomalies close to this one on ");
    text.append(anomaliesOfSameAlertSameEnum.stream()
        .map(a -> a.getStartTime().getTime())
        .map(t -> new DateTime(t).toString(timeFormatter))
        .collect(Collectors.joining(", ")));
    text.append(". ");
    text.append("Maybe those anomalies are related. ");
    return text;
  }

  private StringBuilder textForOtherAnomalies(final List<AnomalyApi> otherAnomalies,
      final RcaInfo rcaInfo) {
    final StringBuilder text = new StringBuilder();
    if (otherAnomalies.isEmpty()) {
      return text;
    }
    final DateTimeFormatter timeFormatter = timeFormatterFor(rcaInfo.granularity(),
        rcaInfo.chronology());
    text.append("Anomalies in different metrics were also detected around the same time for: ");
    for (int i = 0; i < otherAnomalies.size(); i++) {
      final AnomalyApi anomaly = otherAnomalies.get(i);
      final Long anomalyAlertId = anomaly.getAlert().getId();
      final String alertName = optional(alertDAO.findById(anomalyAlertId)).map(AlertDTO::getName)
          .orElse("alertId " + anomalyAlertId);
      text.append(alertName);
      if (anomaly.getEnumerationItem() != null) {
        final Long anomalyEnumerationItemId = anomaly.getEnumerationItem().getId();
        final String enumerationName = optional(
            enumerationItemDAO.findById(anomalyEnumerationItemId)).map(EnumerationItemDTO::getName)
            .orElse("enumerationItemId " + anomalyEnumerationItemId);
        text.append(" - ").append(enumerationName);
      }
      text.append(" on ")
          .append(new DateTime(anomaly.getStartTime().getTime()).toString(timeFormatter));
      if (i < otherAnomalies.size() - 1) {
        text.append(", ");
      }
      if (i >= 2) {
        text.append("and ").append(otherAnomalies.size()).append(" other anomalies");
        break;
      }
    }
    text.append(". ");
    return text;
  }

  private boolean isOfSameAlertSameEnum(final AnomalyDTO anomaly, final AnomalyApi relatedAnomaly) {
    return
        // same alert
        Objects.equals(relatedAnomaly.getAlert().getId(), anomaly.getDetectionConfigId())
            // not a dx alert or same enum
            && (anomaly.getEnumerationItem() == null || Objects.equals(
            anomaly.getEnumerationItem().getId(), relatedAnomaly.getEnumerationItem().getId()));
  }

  private static boolean isOfSameAlertDifferentEnumAndClose(final AnomalyDTO anomaly,
      final AnomalyApi relatedAnomaly) {
    if (anomaly.getEnumerationItem() == null) {
      // not a dx alert
      return false;
    }
    return
        // same time ==> "close"
        // todo implement more fuzzy definition of close --> an overlap is close
        relatedAnomaly.getStartTime().getTime() == anomaly.getStartTime()
            && relatedAnomaly.getEndTime().getTime() == anomaly.getEndTime()
            // same alert
            && Objects.equals(relatedAnomaly.getAlert().getId(), anomaly.getDetectionConfigId())
            // different enum
            // FIXME NOT NULL SAFE
            && !Objects.equals(relatedAnomaly.getEnumerationItem().getId(),
            anomaly.getEnumerationItem().getId());
  }
}
