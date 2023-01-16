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

@Deprecated
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
