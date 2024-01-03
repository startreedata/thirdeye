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
package ai.startree.thirdeye.util;

import java.util.Arrays;
import org.joda.time.Chronology;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class StringUtils {

  /**
   * Copy-pasted from https://www.baeldung.com/java-levenshtein-distance
   */
  public static int levenshteinDistance(final String x, final String y) {
    int[][] dp = new int[x.length() + 1][y.length() + 1];

    for (int i = 0; i <= x.length(); i++) {
      for (int j = 0; j <= y.length(); j++) {
        if (i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
              dp[i - 1][j] + 1, dp[i][j - 1] + 1);
        }
      }
    }

    return dp[x.length()][y.length()];
  }

  private static int costOfSubstitution(char a, char b) {
    return a == b ? 0 : 1;
  }

  private static int min(int... numbers) {
    return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
  }

  /**
   * Returns a time formatter that is as simple as possible to read, depending on the granularity.
   */
  public static DateTimeFormatter timeFormatterFor(final Period granularity,
      final Chronology chronology) {
    if (granularity.equals(Period.days(1))) {
      return DateTimeFormat.forPattern("EEEEE MMMMMM d").withChronology(chronology);
    } else if (granularity.equals(Period.hours(1))) {
      return DateTimeFormat.forPattern("EEEEE MMMMMM d hh aaa").withChronology(chronology);
    }
    return DateTimeFormat.forPattern("EEEEE MMMMMM d HH:mm:ss").withChronology(chronology);
  }
}
