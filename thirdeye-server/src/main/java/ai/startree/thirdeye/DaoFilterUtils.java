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
package ai.startree.thirdeye;

import static ai.startree.thirdeye.ResourceUtils.ensure;
import static ai.startree.thirdeye.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_INVALID_QUERY_PARAM_OPERATOR;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_LIMIT_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_OFFSET_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OFFSET_WITHOUT_LIMIT;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNEXPECTED_QUERY_PARAM;
import static ai.startree.thirdeye.spi.util.Pair.pair;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.util.Pair;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DaoFilterUtils {

  private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("limit", "offset");
  private static final ImmutableMap<String, OPER> OPERATOR_MAP = ImmutableMap.<String, OPER>builder()
      .put("eq", OPER.EQ)
      .put("gt", OPER.GT)
      .put("gte", OPER.GE)
      .put("lt", OPER.LT)
      .put("lte", OPER.LE)
      .put("neq", OPER.NEQ)
      .put("in", OPER.IN)
      .build();
  private static final Pattern PATTERN = Pattern.compile("\\[(\\w+)\\](\\S+)");

  public static DaoFilter buildFilter(
      final MultivaluedMap<String, String> queryParameters,
      final Map<String, String> apiToBeanMap,
      final String namespace) {
    final DaoFilter daoFilter = new DaoFilter();
    optional(queryParameters.getFirst("limit"))
        .map(Long::valueOf)
        .ifPresent( limit -> {
          ensure(limit >= 0, ERR_NEGATIVE_LIMIT_VALUE);
          daoFilter.setLimit(limit);
        });
    optional(queryParameters.getFirst("offset"))
        .map(Long::valueOf)
        .ifPresent(offset -> {
          ensureExists(daoFilter.getLimit(),ERR_OFFSET_WITHOUT_LIMIT);
          ensure(offset >= 0, ERR_NEGATIVE_OFFSET_VALUE);
          daoFilter.setOffset(offset);
        });

    final List<Predicate> predicates = buildPredicates(queryParameters, apiToBeanMap);
    predicates.add(Predicate.EQ("namespace", namespace));
    
    return daoFilter.setPredicate(Predicate.AND(predicates.toArray(new Predicate[]{})));
  }

  private static @NonNull List<Predicate> buildPredicates(
      final MultivaluedMap<String, String> queryParameters,
      final Map<String, String> apiToBeanMap) {
    final List<Predicate> predicates = new ArrayList<>();
    for (Map.Entry<String, List<String>> e : queryParameters.entrySet()) {
      final String qParam = e.getKey();
      if (KEYWORDS.contains(qParam)) {
        continue;
      }
      final String columnName = ensureExists(
          apiToBeanMap.get(qParam),
          ERR_UNEXPECTED_QUERY_PARAM,
          apiToBeanMap.keySet());
      final Object[] objects = e.getValue().toArray();
      predicates.add(toPredicate(columnName, objects));
    }
    return predicates;
  }
  
  @VisibleForTesting
  protected static Pair<OPER, String> toPair(final Object o) {
    final String s = o.toString();

    final Matcher m = PATTERN.matcher(s);
    if (m.matches()) {
      final OPER operator = OPERATOR_MAP.get(m.group(1));
      if (operator == null) {
        throw new ThirdEyeException(ERR_INVALID_QUERY_PARAM_OPERATOR, OPERATOR_MAP.keySet());
      }
      return pair(operator, m.group(2));
    }
    return pair(OPER.EQ, s);
  }

  @VisibleForTesting
  protected static Predicate toPredicate(final String columnName, final Object[] objects) {
    final List<Predicate> predicates = Arrays.stream(objects)
        .map(DaoFilterUtils::toPair)
        .map(p -> new Predicate(columnName, p.getFirst(), p.getSecond()))
        .toList();

    return Predicate.AND(predicates.toArray(new Predicate[]{}));
  }
}