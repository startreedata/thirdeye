/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_INVALID_QUERY_PARAM_OPERATOR;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNEXPECTED_QUERY_PARAM;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.util.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;

public class DaoFilterBuilder {

  private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("limit");
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
  private final ImmutableMap<String, String> apiToBeanMap;

  public DaoFilterBuilder(final ImmutableMap<String, String> apiToBeanMap) {
    this.apiToBeanMap = apiToBeanMap;
  }

  static Pair<OPER, String> toPair(final Object o) {
    final String s = o.toString();

    final Matcher m = PATTERN.matcher(s);
    if (m.matches()) {
      final OPER operator = OPERATOR_MAP.get(m.group(1));
      if (operator == null) {
        throw new ThirdEyeException(ERR_INVALID_QUERY_PARAM_OPERATOR, OPERATOR_MAP.keySet());
      }
      return Pair.createPair(operator, m.group(2));
    }
    return Pair.createPair(OPER.EQ, s);
  }

  static Predicate toPredicate(final String columnName, final Object[] objects) {
    final List<Predicate> predicates = Arrays.stream(objects)
        .map(DaoFilterBuilder::toPair)
        .map(p -> new Predicate(columnName, p.getFirst(), p.getSecond()))
        .collect(Collectors.toList());

    return Predicate.AND(predicates.toArray(new Predicate[]{}));
  }

  public DaoFilter buildFilter(final MultivaluedMap<String, String> queryParameters) {
    final DaoFilter daoFilter = new DaoFilter();
    optional(queryParameters.getFirst("limit"))
        .map(Integer::valueOf)
        .ifPresent(daoFilter::setLimit);

    return daoFilter.setPredicate(buildPredicate(queryParameters));
  }

  private Predicate buildPredicate(final MultivaluedMap<String, String> queryParameters) {
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
    return Predicate.AND(predicates.toArray(new Predicate[]{}));
  }
}
