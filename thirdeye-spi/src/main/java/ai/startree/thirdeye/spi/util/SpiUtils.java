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

import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
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
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.PeriodType;

public class SpiUtils {

  public static final String FILTER_VALUE_ASSIGNMENT_SEPARATOR = "=";
  public static final String FILTER_CLAUSE_SEPARATOR = ";";
  private static final Splitter SEMICOLON_SPLITTER = Splitter.on(";").omitEmptyStrings();
  private static final Splitter EQUALS_SPLITTER = Splitter.on("=").omitEmptyStrings();
  private static final Joiner SEMICOLON = Joiner.on(";");
  private static final Joiner EQUALS = Joiner.on("=");

  private SpiUtils() {
  }

  public static <T> Optional<T> optional(@Nullable final T o) {
    return Optional.ofNullable(o);
  }

  public static boolean bool(final Boolean value) {
    return value != null && value;
  }

  public static String constructMetricAlias(final String datasetName, final String metricName) {
    return datasetName + MetricConfigDTO.ALIAS_JOINER + metricName;
  }

  /**
   * Returns partial to zero out date fields based on period type
   *
   * @return partial
   */
  public static Partial makeOrigin(final PeriodType type) {
    final List<DateTimeFieldType> fields = new ArrayList<>();

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

    final int[] zeros = new int[fields.size()];
    Arrays.fill(zeros, 0);

    // workaround for dayOfMonth > 0 constraint
    if (PeriodType.months().equals(type)) {
      zeros[0] = 1;
    }

    return new Partial(fields.toArray(new DateTimeFieldType[fields.size()]), zeros);
  }

  public static Multimap<String, String> getFilterSet(final String filters) {
    final Multimap<String, String> filterSet = ArrayListMultimap.create();
    if (StringUtils.isNotBlank(filters)) {
      final String[] filterClauses = filters.split(FILTER_CLAUSE_SEPARATOR);
      for (final String filterClause : filterClauses) {
        final String[] values = filterClause.split(FILTER_VALUE_ASSIGNMENT_SEPARATOR, 2);
        if (values.length != 2) {
          throw new IllegalArgumentException(
              "Filter values assigments should in pairs: " + filters);
        }
        filterSet.put(values[0], values[1]);
      }
    }
    return filterSet;
  }

  public static String getSortedFiltersFromMultiMap(final Multimap<String, String> filterMultiMap) {
    final Set<String> filterKeySet = filterMultiMap.keySet();
    final ArrayList<String> filterKeyList = new ArrayList<>(filterKeySet);
    Collections.sort(filterKeyList);

    final StringBuilder sb = new StringBuilder();
    for (final String filterKey : filterKeyList) {
      final ArrayList<String> values = new ArrayList<>(filterMultiMap.get(filterKey));
      Collections.sort(values);
      for (final String value : values) {
        sb.append(filterKey);
        sb.append(FILTER_VALUE_ASSIGNMENT_SEPARATOR);
        sb.append(value);
        sb.append(FILTER_CLAUSE_SEPARATOR);
      }
    }
    return StringUtils.chop(sb.toString());
  }

  public static String getSortedFilters(final String filters) {
    final Multimap<String, String> filterMultiMap = getFilterSet(filters);
    final String sortedFilters = getSortedFiltersFromMultiMap(filterMultiMap);

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
  public static String encodeCompactedProperties(final Properties props) {
    final List<String> parts = new ArrayList<>();
    for (final Map.Entry<Object, Object> entry : props.entrySet()) {
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
  public static Properties decodeCompactedProperties(final String propStr) {
    final Properties props = new Properties();
    for (final String part : SEMICOLON_SPLITTER.split(propStr)) {
      final List<String> kvPair = EQUALS_SPLITTER.splitToList(part);
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

  public static AlertDTO alertRef(final Long alertId) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(alertId);
    return alert;
  }

  public static EnumerationItemDTO enumerationItemRef(final long id) {
    final EnumerationItemDTO enumerationItemDTO = new EnumerationItemDTO();
    enumerationItemDTO.setId(id);
    return enumerationItemDTO;
  }

  public enum TimeFormat {
    EPOCH, SIMPLE_DATE_FORMAT
  }
}
