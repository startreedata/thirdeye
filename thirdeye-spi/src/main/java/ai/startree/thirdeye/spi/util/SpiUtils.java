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
package ai.startree.thirdeye.spi.util;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Partial;
import org.joda.time.PeriodType;

public class SpiUtils {

  public static final String FILTER_VALUE_ASSIGNMENT_SEPARATOR = "=";
  public static final String FILTER_CLAUSE_SEPARATOR = ";";
  public static final Pattern PATTERN_FILTER_OPERATOR = Pattern.compile("!=|>=|<=|==|>|<|=");
  private static final Splitter SEMICOLON_SPLITTER = Splitter.on(";").omitEmptyStrings();
  private static final Splitter EQUALS_SPLITTER = Splitter.on("=").omitEmptyStrings();
  private static final Joiner SEMICOLON = Joiner.on(";");
  private static final Joiner EQUALS = Joiner.on("=");

  private SpiUtils() {
  }

  public static <T> Optional<T> optional(T o) {
    return Optional.ofNullable(o);
  }

  public static boolean bool(Boolean value) {
    return value != null && value;
  }

  public static String constructMetricAlias(String datasetName, String metricName) {
    return datasetName + MetricConfigDTO.ALIAS_JOINER + metricName;
  }

  public static String getTimeFormatString(DatasetConfigDTO datasetConfig) {
    String timeFormat = datasetConfig.getTimeFormat();
    if (timeFormat.startsWith(TimeFormat.SIMPLE_DATE_FORMAT.toString())) {
      timeFormat = getSDFPatternFromTimeFormat(timeFormat);
    }
    return timeFormat;
  }

  private static String getSDFPatternFromTimeFormat(String timeFormat) {
    String pattern = timeFormat;
    String[] tokens = timeFormat.split(":", 2);
    if (tokens.length == 2) {
      pattern = tokens[1];
    }
    return pattern;
  }

  public static List<Range<DateTime>> computeTimeRanges(TimeGranularity granularity, DateTime start,
      DateTime end) {
    List<Range<DateTime>> timeranges = new ArrayList<>();
    if (granularity == null) {
      timeranges.add(Range.closedOpen(start, end));
      return timeranges;
    }
    DateTime current = start;
    DateTime newCurrent;
    while (current.isBefore(end)) {
      newCurrent = increment(current, granularity);
      timeranges.add(Range.closedOpen(current, newCurrent));
      current = newCurrent;
    }
    return timeranges;
  }

  public static DateTime increment(DateTime input, TimeGranularity granularity) {
    DateTime output;
    switch (granularity.getUnit()) {
      case DAYS:
        output = input.plusDays(granularity.getSize());
        break;
      case HOURS:
        output = input.plusHours(granularity.getSize());
        break;
      case MILLISECONDS:
        output = input.plusMillis(granularity.getSize());
        break;
      case MINUTES:
        output = input.plusMinutes(granularity.getSize());
        break;
      case SECONDS:
        output = input.plusSeconds(granularity.getSize());
        break;
      default:
        throw new IllegalArgumentException("Timegranularity:" + granularity + " not supported");
    }
    return output;
  }

  /**
   * Returns partial to zero out date fields based on period type
   *
   * @return partial
   */
  public static Partial makeOrigin(PeriodType type) {
    List<DateTimeFieldType> fields = new ArrayList<>();

    if (PeriodType.millis().equals(type)) {
      // left blank

    } else if (PeriodType.seconds().equals(type)) {
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.minutes().equals(type)) {
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.hours().equals(type)) {
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.days().equals(type)) {
      fields.add(DateTimeFieldType.hourOfDay());
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.months().equals(type)) {
      fields.add(DateTimeFieldType.dayOfMonth());
      fields.add(DateTimeFieldType.hourOfDay());
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else {
      throw new IllegalArgumentException(String.format("Unsupported PeriodType '%s'", type));
    }

    int[] zeros = new int[fields.size()];
    Arrays.fill(zeros, 0);

    // workaround for dayOfMonth > 0 constraint
    if (PeriodType.months().equals(type)) {
      zeros[0] = 1;
    }

    return new Partial(fields.toArray(new DateTimeFieldType[fields.size()]), zeros);
  }

  public static DateTimeZone getDateTimeZone(final DatasetConfigDTO datasetConfig) {
    final String timezone = datasetConfig != null
        ? datasetConfig.getTimezone()
        : Constants.DEFAULT_TIMEZONE_STRING;
    return DateTimeZone.forID(timezone);
  }

  /**
   * Given time granularity and start time (with local time zone information), returns the bucket
   * index of the current time (with local time zone information).
   *
   * The reason to use this method to calculate the bucket index is to align the shifted data point
   * due to daylight saving time to the correct bucket index. Note that this method have no effect
   * if the input time use UTC timezone.
   *
   * For instance, considering March 13th 2016, the day DST takes effect. Assume that our daily
   * data whose timestamp is aligned at 0 am at each day, then the data point on March 14th would
   * be actually aligned to 13th's bucket. Because the two data point only has 23 hours difference.
   * Therefore, we cannot calculate the bucket index simply divide the difference between timestamps
   * by millis of 24 hours.
   *
   * We don't need to consider the case of HOURS because the size of a bucket does not change when
   * the time granularity is smaller than DAYS. In DAYS, the bucket size could be 23, 24, or 25
   * hours due to DST. In HOURS or anything smaller, the bucket size does not change. Hence, we
   * simply compute the bucket index using one fixed bucket size (i.e., interval).
   *
   * @param granularity the time granularity of the bucket
   * @param start the start time of the first bucket
   * @param current the current time
   * @return the bucket index of current time
   */
  public static int computeBucketIndex(TimeGranularity granularity, DateTime start,
      DateTime current) {
    int index;
    switch (granularity.getUnit()) {
      case DAYS:
        Days d = Days.daysBetween(start, current);
        index = d.getDays() / granularity.getSize();
        break;
      default:
        long interval = granularity.toMillis();
        index = (int) ((current.getMillis() - start.getMillis()) / interval);
    }
    return index;
  }

  public static Multimap<String, String> getFilterSet(String filters) {
    Multimap<String, String> filterSet = ArrayListMultimap.create();
    if (StringUtils.isNotBlank(filters)) {
      String[] filterClauses = filters.split(FILTER_CLAUSE_SEPARATOR);
      for (String filterClause : filterClauses) {
        String[] values = filterClause.split(FILTER_VALUE_ASSIGNMENT_SEPARATOR, 2);
        if (values.length != 2) {
          throw new IllegalArgumentException(
              "Filter values assigments should in pairs: " + filters);
        }
        filterSet.put(values[0], values[1]);
      }
    }
    return filterSet;
  }

  public static String getSortedFiltersFromMultiMap(Multimap<String, String> filterMultiMap) {
    Set<String> filterKeySet = filterMultiMap.keySet();
    ArrayList<String> filterKeyList = new ArrayList<>(filterKeySet);
    Collections.sort(filterKeyList);

    StringBuilder sb = new StringBuilder();
    for (String filterKey : filterKeyList) {
      ArrayList<String> values = new ArrayList<>(filterMultiMap.get(filterKey));
      Collections.sort(values);
      for (String value : values) {
        sb.append(filterKey);
        sb.append(FILTER_VALUE_ASSIGNMENT_SEPARATOR);
        sb.append(value);
        sb.append(FILTER_CLAUSE_SEPARATOR);
      }
    }
    return StringUtils.chop(sb.toString());
  }

  public static String getSortedFilters(String filters) {
    Multimap<String, String> filterMultiMap = getFilterSet(filters);
    String sortedFilters = getSortedFiltersFromMultiMap(filterMultiMap);

    if (StringUtils.isBlank(sortedFilters)) {
      return null;
    }
    return sortedFilters;
  }

  /**
   * Encode the properties into String, which is in the format of field1=value1;field2=value2 and so
   * on
   *
   * @param props the properties instance
   * @return a String representation of the given properties
   */
  public static String encodeCompactedProperties(Properties props) {
    List<String> parts = new ArrayList<>();
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      parts.add(EQUALS.join(entry.getKey(), entry.getValue()));
    }
    return SEMICOLON.join(parts);
  }

  /**
   * Decode the properties string (e.g. field1=value1;field2=value2) to a properties instance
   *
   * @param propStr a properties string in the format of field1=value1;field2=value2
   * @return a properties instance
   */
  public static Properties decodeCompactedProperties(String propStr) {
    Properties props = new Properties();
    for (String part : SEMICOLON_SPLITTER.split(propStr)) {
      List<String> kvPair = EQUALS_SPLITTER.splitToList(part);
      if (kvPair.size() == 2) {
        props.setProperty(kvPair.get(0).trim(), kvPair.get(1).trim());
      } else if (kvPair.size() == 1) {
        props.setProperty(kvPair.get(0).trim(), "");
      } else {
        throw new IllegalArgumentException(part + " is not a legal property string");
      }
    }
    return props;
  }

  /**
   * check if the metric aggregation is cumulative
   */
  public static boolean isAggCumulative(MetricConfigDTO metric) {
    final MetricAggFunction aggFunction = MetricAggFunction.fromString(metric.getDefaultAggFunction());
    return aggFunction.equals(MetricAggFunction.SUM) || aggFunction.equals(MetricAggFunction.COUNT);
  }

  /**
   * Returns the filter predicate for a given filter string
   *
   * <br/><b>Example:</b>
   * <pre>
   *   >> "country != us"
   *   << {"country", "!=", "us"}
   * </pre>
   *
   * @param filterString raw (decoded) filter string
   * @return filter predicate
   */
  // todo cyril return a Predicate
  public static FilterPredicate extractFilterPredicate(String filterString) {
    Matcher m = PATTERN_FILTER_OPERATOR.matcher(filterString);
    if (!m.find()) {
      throw new IllegalArgumentException(
          String.format("Could not find filter predicate operator. Expected regex '%s'",
              PATTERN_FILTER_OPERATOR.pattern()));
    }

    int keyStart = 0;
    int keyEnd = m.start();
    String key = filterString.substring(keyStart, keyEnd);

    int opStart = m.start();
    int opEnd = m.end();
    String operator = filterString.substring(opStart, opEnd);

    int valueStart = m.end();
    int valueEnd = filterString.length();
    String value = filterString.substring(valueStart, valueEnd);

    return new FilterPredicate(key, operator, value);
  }

  public static List<FilterPredicate> extractFilterPredicates(List<String> filters) {
    return filters.stream().map(SpiUtils::extractFilterPredicate).collect(Collectors.toList());
  }

  public static boolean isFilterOperatorExists(String any) {
    Matcher m = PATTERN_FILTER_OPERATOR.matcher(any);
    return m.find();
  }

  public enum TimeFormat {
    EPOCH, SIMPLE_DATE_FORMAT
  }
}
