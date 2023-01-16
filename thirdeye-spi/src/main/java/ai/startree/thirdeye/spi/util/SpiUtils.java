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
package ai.startree.thirdeye.spi.util;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTimeFieldType;
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

  public static <T> Optional<T> optional(@Nullable T o) {
    return Optional.ofNullable(o);
  }

  public static boolean bool(Boolean value) {
    return value != null && value;
  }

  public static String constructMetricAlias(String datasetName, String metricName) {
    return datasetName + MetricConfigDTO.ALIAS_JOINER + metricName;
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

  public enum TimeFormat {
    EPOCH, SIMPLE_DATE_FORMAT
  }
}
