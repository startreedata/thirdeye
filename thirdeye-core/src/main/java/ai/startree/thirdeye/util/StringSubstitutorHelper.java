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

package ai.startree.thirdeye.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringSubstitutorHelper {

  private StringSubstitutorHelper() {
  }

  /**
   * Escape variables that are recursively used in replacement.
   * Example
   * "min": "${min}"
   *
   * By default this raises an error with apache commons. This function escapes the use of recursive
   * variables by escaping using "$"
   * In this case, the string is replaced as follows
   * "min": "$${min}"
   *
   * NOTE!!! Escape String variables only. No other types are supported
   *
   * @param valuesMap values map fed into {@link org.apache.commons.text.StringSubstitutor}
   * @return copy of input map with values replaced with escaped sequences if required
   */
  static Map<String, Object> escapeRecursiveVariables(final Map<String, Object> valuesMap) {
    final Set<String> keys = valuesMap.keySet().stream()
        .map(s -> String.format("${%s}", s))
        .collect(Collectors.toSet());

    final Map<String, Object> newValues = new HashMap<>(valuesMap.size());
    for (final var e : valuesMap.entrySet()) {
      final Object v = e.getValue();

      /* Escape String variables only. No other types are supported */
      newValues.put(e.getKey(), v instanceof String
          ? escapeIfReqd((String) v, keys)
          : v);
    }
    return newValues;
  }

  /**
   * @param s String to be escaped
   * @param keys list of boxed variables with ${}. Example: Set.of("${v1}", "${v2}") etc
   * @return escaped String
   */
  static String escapeIfReqd(final String s, final Set<String> keys) {
    String replaced = s;
    for (String k : keys) {
      // Replace ${v1} with $${v1} but leave already escaped values alone $${v1}
      final String regex = "(?<!\\$)" + Pattern.quote(k);
      final Matcher matcher = Pattern.compile(regex).matcher(replaced);
      replaced = matcher.replaceAll(Matcher.quoteReplacement("$" + k));
    }
    return replaced;
  }
}
