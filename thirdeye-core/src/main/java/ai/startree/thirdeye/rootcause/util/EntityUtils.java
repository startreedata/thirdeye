/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.util;

import ai.startree.thirdeye.rootcause.entity.EntityType;
import ai.startree.thirdeye.spi.util.FilterPredicate;
import ai.startree.thirdeye.spi.util.SpiUtils;
import ai.startree.thirdeye.util.ParsedUrn;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class to simplify type-checking and extraction of entities
 */
public class EntityUtils {

  private static final Map<String, String> FILTER_TO_OPERATOR = new LinkedHashMap<>();

  static {
    FILTER_TO_OPERATOR.put("!", "!=");
    FILTER_TO_OPERATOR.put("<=", "<=");
    FILTER_TO_OPERATOR.put("<", "<");
    FILTER_TO_OPERATOR.put(">=", ">=");
    FILTER_TO_OPERATOR.put(">", ">");
  }

  /**
   * Decode URN fragment to original data.
   * <br/><b>NOTE:</b> compatible with JavaScript's decodeURIComponent
   *
   * @param value urn fragment value
   * @return decoded value
   */
  public static String decodeURNComponent(String value) {
    try {
      return URLDecoder.decode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // must not happen, utf-8 is part of java spec
      throw new IllegalStateException(e);
    }
  }

  /**
   * Encode data to URN fragment.
   * <br/><b>NOTE:</b> similar to JavaScript's encodeURIComponent for basic ascii set
   *
   * @param value value
   * @return encoded urn fragment
   */
  public static String encodeURNComponent(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8")
          .replace("+", "%20")
          .replace("%21", "!")
          .replace("%27", "'")
          .replace("%28", "(")
          .replace("%29", ")")
          .replace("%7E", "~");
    } catch (UnsupportedEncodingException e) {
      // must not happen, utf-8 is part of java spec
      throw new IllegalStateException(e);
    }
  }

  /**
   * Decodes filter string fragments to a dimensions multimap
   *
   * @param filterStrings dimension fragments
   * @return dimensions multimap
   */
  public static Multimap<String, String> decodeDimensions(List<String> filterStrings) {
    Multimap<String, String> filters = TreeMultimap.create();

    for (String filterString : filterStrings) {
      if (StringUtils.isBlank(filterString)) {
        continue;
      }

      String[] parts = EntityUtils.decodeURNComponent(filterString).split("!=|<=|>=|<|>|=", 2);
      if (parts.length != 2) {
        throw new IllegalArgumentException(
            String.format("Could not parse filter string '%s'", filterString));
      }

      filters.put(parts[0], parts[1]);
    }

    return filters;
  }

  /**
   * Encodes dimensions multimap to filter strings.
   *
   * @param filters dimensions multimap
   * @return filter string fragments
   */
  public static List<String> encodeDimensions(Multimap<String, String> filters) {
    List<String> output = new ArrayList<>();

    Multimap<String, String> sorted = TreeMultimap.create(filters);
    for (Map.Entry<String, String> entry : sorted.entries()) {
      String operator = "=";
      String value = entry.getValue();

      // check for exclusion case
      for (Map.Entry<String, String> prefix : FILTER_TO_OPERATOR.entrySet()) {
        if (entry.getValue().startsWith(prefix.getKey())) {
          operator = prefix.getValue();
          value = entry.getValue().substring(prefix.getKey().length());
          break;
        }
      }

      output.add(
          EntityUtils.encodeURNComponent(String.format("%s%s%s", entry.getKey(), operator, value)));
    }

    return output;
  }

  /**
   * Returns the parsed urn without filters.
   *
   * @param urn entity urn string
   * @return ParsedUrn
   */
  public static ParsedUrn parseUrnString(String urn) {
    return new ParsedUrn(Arrays.asList(urn.split(":")), Collections.emptySet());
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
   * @return ParsedUrn
   */
  public static ParsedUrn parseUrnString(String urn, EntityType type) {
    if (!type.isType(urn)) {
      throw new IllegalArgumentException(
          String.format("Expected type '%s' but got '%s'", type.getPrefix(), urn));
    }
    return parseUrnString(urn);
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
