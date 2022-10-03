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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.plugins.postprocessor.LabelUtils.addLabel;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;

public class TimeOfWeekPostProcessor implements AnomalyPostProcessor<TimeOfWeekPostProcessorSpec> {

  private final static Set<Integer> DEFAULT_INT_DAYS_OF_WEEK = Set.of();
  private final static Set<Integer> DEFAULT_HOURS_OF_DAY = Set.of();
  private final static Map<Integer, Set<Integer>> DEFAULT_INT_DAY_HOURS_OF_WEEK = Map.of();

  // see package org.joda.time.DateTimeConstants;
  private final static Map<String, Integer> DAY_STRING_TO_JODA_INT = ImmutableMap.<String, Integer>builder()
      .put("MONDAY", 1)
      .put("TUESDAY", 2)
      .put("WEDNESDAY", 3)
      .put("THURSDAY", 4)
      .put("FRIDAY", 5)
      .put("SATURDAY", 6)
      .put("SUNDAY", 7)
      .build();

  private final static Set<String> VALID_DAYS = DAY_STRING_TO_JODA_INT.keySet();

  private Set<Integer> intDaysOfWeek;
  private Set<Integer> hoursOfDay;
  private Map<Integer, Set<Integer>> intDayHoursOfWeek;

  private final boolean DEFAULT_IGNORE = false;

  private boolean ignore;

  private String labelName;

  @Override
  public void init(final TimeOfWeekPostProcessorSpec spec) {
    this.ignore = optional(spec.getIgnore()).orElse(DEFAULT_IGNORE);

    this.intDaysOfWeek = optional(spec.getDaysOfWeek()).map(l -> l.stream()
        .map(TimeOfWeekPostProcessor::dayStringToDayInt)
        .collect(Collectors.toSet())).orElse(DEFAULT_INT_DAYS_OF_WEEK);
    this.hoursOfDay = optional(spec.getHoursOfDay()).map(TimeOfWeekPostProcessor::parseHours)
        .orElse(DEFAULT_HOURS_OF_DAY);
    this.intDayHoursOfWeek = parseDayHoursOfWeek(spec.getDayHoursOfWeek());

    this.labelName = labelName(spec.getDaysOfWeek(),
        spec.getHoursOfDay(),
        spec.getDayHoursOfWeek());
  }

  @Override
  public Class<TimeOfWeekPostProcessorSpec> specClass() {
    return TimeOfWeekPostProcessorSpec.class;
  }

  @Override
  public String name() {
    return "TimeOfWeek";
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {
    // short-circuit if no special times of week
    if (intDaysOfWeek.size() == 0 && hoursOfDay.size() == 0 && intDayHoursOfWeek.size() == 0) {
      return resultMap;
    }

    final Chronology chronology = detectionInterval.getChronology();
    for (final OperatorResult operatorResult : resultMap.values()) {
      postProcessResult(operatorResult, chronology);
    }

    return resultMap;
  }

  private void postProcessResult(@NonNull final OperatorResult operatorResult,
      final Chronology chronology) {
    // todo cyril default implementation of getAnomalies throws error - obliged to catch here - change default implem?
    try {
      final List<MergedAnomalyResultDTO> anomalies = operatorResult.getAnomalies();
      for (final MergedAnomalyResultDTO anomalyResultDTO : anomalies) {
        // labeling is just based on the startTime - this may be counter-intuitive for anomalies with length > 1 granularity bucket
        final DateTime startTime = new DateTime(anomalyResultDTO.getStartTime(), chronology);
        final int anomalyDayOfWeek = startTime.getDayOfWeek();
        final int anomalyHourOfDay = startTime.getHourOfDay();
        final boolean dayMatch = intDaysOfWeek.contains(anomalyDayOfWeek);
        final boolean hourMatch = hoursOfDay.contains(anomalyHourOfDay);
        final Set<Integer> hoursOfAnomalyDay = intDayHoursOfWeek.get(anomalyDayOfWeek);
        final boolean dayHourMatch =
            hoursOfAnomalyDay != null && hoursOfAnomalyDay.contains(anomalyHourOfDay);
        if (dayMatch || hourMatch || dayHourMatch) {
          final AnomalyLabelDTO newLabel = new AnomalyLabelDTO().setIgnore(ignore)
              .setName(labelName);
          addLabel(anomalyResultDTO, newLabel);
        }
      }
    } catch (final UnsupportedOperationException e) {
      // no anomalies - continue
    }
  }

  private static Map<Integer, Set<Integer>> parseDayHoursOfWeek(
      @Nullable final Map<String, List<Integer>> dayHoursOfWeek) {
    if (dayHoursOfWeek == null) {
      return DEFAULT_INT_DAY_HOURS_OF_WEEK;
    }
    final Map<Integer, Set<Integer>> intDaysHoursOfWeek = new HashMap<>();
    for (final Entry<String, List<Integer>> entry : dayHoursOfWeek.entrySet()) {
      final Integer intDay = dayStringToDayInt(entry.getKey());
      intDaysHoursOfWeek.put(intDay, parseHours(Objects.requireNonNull(entry.getValue())));
    }

    return intDaysHoursOfWeek;
  }

  private static Set<Integer> parseHours(@NonNull final List<Integer> hours) {
    for (final Integer hour : hours) {
      checkArgument(hour >= 0 && hour < 24, "Invalid hour: %s. Valid hour in [0,23].", hour);
    }
    return Set.copyOf(hours);
  }

  private static Integer dayStringToDayInt(final String day) {
    final String preparedString = day.toUpperCase(Locale.ENGLISH);
    checkArgument(VALID_DAYS.contains(preparedString),
        "Invalid day string: %s. Valid day strings: %s",
        day,
        VALID_DAYS);
    return DAY_STRING_TO_JODA_INT.get(preparedString);
  }

  @VisibleForTesting
  protected static String labelName(@Nullable final List<String> intDaysOfWeek,
      @Nullable final List<Integer> hoursOfDay,
      @Nullable final Map<String, List<Integer>> dayHoursOfWeek) {
    String label = "Time of Week";
    if (intDaysOfWeek != null && intDaysOfWeek.size() > 0) {
      label += "|Days " + intDaysOfWeek;
    }
    if (hoursOfDay != null && hoursOfDay.size() > 0) {
      label += "|Hours " + hoursOfDay;
    }
    if (dayHoursOfWeek != null && dayHoursOfWeek.size() > 0) {
      label += "|Day-Hours " + dayHoursOfWeek;
    }
    return label;
  }
}
