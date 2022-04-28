/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.util.FilterPredicate;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ParsedUrn {

  private static final Map<String, String> OPERATOR_TO_FILTER = new HashMap<>();

  static {
    OPERATOR_TO_FILTER.put("!=", "!");
    OPERATOR_TO_FILTER.put("==", "");
    OPERATOR_TO_FILTER.put("=", "");
    OPERATOR_TO_FILTER.put("<=", "<=");
    OPERATOR_TO_FILTER.put("<", "<");
    OPERATOR_TO_FILTER.put(">=", ">=");
    OPERATOR_TO_FILTER.put(">", ">");
  }

  final List<String> prefixes;
  final Set<FilterPredicate> predicates;

  public ParsedUrn(List<String> prefixes) {
    this.prefixes = prefixes;
    this.predicates = Collections.emptySet();
  }

  public ParsedUrn(List<String> prefixes, Set<FilterPredicate> predicates) {
    this.prefixes = Collections.unmodifiableList(prefixes);
    this.predicates = Collections.unmodifiableSet(predicates);
  }

  public List<String> getPrefixes() {
    return prefixes;
  }

  public Set<FilterPredicate> getPredicates() {
    return predicates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParsedUrn parsedUrn = (ParsedUrn) o;
    return Objects.equals(prefixes, parsedUrn.prefixes) && Objects
        .equals(predicates, parsedUrn.predicates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prefixes, predicates);
  }

  /**
   * Convenience method to assert absence of filter predicates at runtime.
   *
   * @throws IllegalArgumentException if at least one filter predicate is present
   */
  public void assertPrefixOnly() throws IllegalArgumentException {
    if (!this.getPredicates().isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Expected prefix only but got predicates %s", this.getPredicates()));
    }
  }

  /**
   * Return FilterPredicates as filters multimap.
   * @deprecated Prefer manipulating FilterPredicate directly with getPredicates()
   * @return filter multimap from predicates
   */
  // prefer Predicate in all ThirdEye - see buildFilter in CrudResource
  @Deprecated
  public Multimap<String, String> toFiltersMap() {
    Multimap<String, String> filters = TreeMultimap.create();
    for (FilterPredicate predicate : this.predicates) {
      if (!OPERATOR_TO_FILTER.containsKey(predicate.getOperator())) {
        throw new IllegalArgumentException(String
            .format("Operator '%s' could not be translated to filter prefix",
                predicate.getOperator()));
      }
      String prefix = OPERATOR_TO_FILTER.get(predicate.getOperator());
      filters.put(predicate.getKey(), prefix + predicate.getValue());
    }
    return filters;
  }
}
