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
package ai.startree.thirdeye.rootcause.util;

import ai.startree.thirdeye.rootcause.entity.EntityType;
import ai.startree.thirdeye.spi.util.FilterPredicate;
import ai.startree.thirdeye.spi.util.SpiUtils;
import ai.startree.thirdeye.util.ParsedUrn;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to simplify type-checking and extraction of entities
 */
public class EntityUtils {

  /**
   * Decode URN fragment to original data.
   * <br/><b>NOTE:</b> compatible with JavaScript's decodeURIComponent
   *
   * @param value urn fragment value
   * @return decoded value
   */
  public static String decodeURNComponent(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  /**
   * Returns the parsed urn for a given filter start offset. Handles ambiguous filter/urn values.
   *
   * <br/><b>Example:</b>
   * <pre>
   *   >> "thirdeye:metric:123:filter!=double:colon"
   *   << {["thirdeye", "metric", "123"], {"filter", "!=", "double:colon"}}
   * </pre>
   *
   * @param urn entity urn string
   * @param filterOffset start offset for filter values
   * @return ParsedUrn
   */
  public static ParsedUrn parseUrnString(String urn, int filterOffset) {
    String[] parts = urn.split(":", filterOffset + 1);

    // leading fragments are copied as-is
    List<String> prefixes = Arrays.asList(Arrays.copyOf(parts, filterOffset));

    // filter fragment (last fragment) is parsed back to front
    Set<FilterPredicate> predicates = new HashSet<>();

    if (parts.length > filterOffset && !parts[filterOffset].isEmpty()) {
      String[] filterFragments = parts[filterOffset].split(":");

      String currentFragment = decodeURNComponent(filterFragments[filterFragments.length - 1]);
      for (int i = filterFragments.length - 1; i > 0; i--) {
        if (currentFragment.isEmpty()) {
          // skip empty filter fragment, retain separator
          currentFragment = ":" + currentFragment;
          continue;
        }

        try {
          // attempt parsing current filter fragment
          predicates.add(SpiUtils.extractFilterPredicate(currentFragment));
          currentFragment = decodeURNComponent(filterFragments[i - 1]);
        } catch (IllegalArgumentException ignore) {
          // merge filter fragment with next if it doesn't parse
          currentFragment = String
              .format("%s:%s", decodeURNComponent(filterFragments[i - 1]), currentFragment);
        }
      }

      // last (combined) filter fragment must parse
      predicates.add(SpiUtils.extractFilterPredicate(currentFragment));
    }

    return new ParsedUrn(prefixes, predicates);
  }


  /**
   * Return the urn parsed for the given type. Validates whether urn matches type and extracts
   * optional filter tail.
   *
   * @param urn entity urn string
   * @param type expected entity type
   * @param filterOffset start offset for filter values
   * @return ParsedUrn
   */
  public static ParsedUrn parseUrnString(String urn, EntityType type, int filterOffset) {
    if (urn == null || !type.isType(urn)) {
      throw new IllegalArgumentException(
          String.format("Expected type '%s' but got '%s'", type.getPrefix(), urn));
    }
    return parseUrnString(urn, filterOffset);
  }
}
