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

import static ai.startree.thirdeye.util.StringSubstitutorHelper.escapeIfReqd;
import static ai.startree.thirdeye.util.StringSubstitutorHelper.escapeRecursiveVariables;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import java.util.Set;
import org.testng.annotations.Test;

public class StringSubstitutorHelperTest {

  @Test
  public void testEscapeIfReqd() {
    assertThat(escapeIfReqd("${v1}", Set.of("${v1}"))).isEqualTo("$${v1}");
    assertThat(escapeIfReqd("$${v1} ${v1} $${v1}", Set.of("${v1}"))).isEqualTo("$${v1} $${v1} $${v1}");
    assertThat(escapeIfReqd("${v1} ${v1} $${v1}", Set.of("${v1}"))).isEqualTo("$${v1} $${v1} $${v1}");
    assertThat(escapeIfReqd("$${v1}", Set.of("${v1}"))).isEqualTo("$${v1}");
    assertThat(escapeIfReqd("${v1} ${v2}", Set.of("${v1}", "${v2}"))).isEqualTo("$${v1} $${v2}");
    assertThat(escapeIfReqd("${v1} ${v2}", Set.of("${v1}"))).isEqualTo("$${v1} ${v2}");

    assertThat(escapeIfReqd(
        "Some text with variable ${abcd}",
        Set.of("${abcd}", "${efgh}", "${Abcd}", "${Abcd.}")))
        .isEqualTo("Some text with variable $${abcd}");

  }

  @Test
  public void testEscapeRecursiveVariables() {
    // basic case recursive variables should be escaped
    assertThat(escapeRecursiveVariables(Map.of(
        "v1", "some string with v1. this will not be escaped",
        "v2", "This should be escaped ${v1} ${v2}"
    )))
        .isEqualTo(Map.of(
            "v1", "some string with v1. this will not be escaped",
            "v2", "This should be escaped $${v1} $${v2}"
        ));

    assertThat(escapeRecursiveVariables(Map.of(
        "v1", "some string with v1. this will not be escaped",
        "v2", "This should be escaped ${v1} ${v2}",
        "v3", "${v3}",
        "v4", "whatever some new var ${v5}"
    )))
        .isEqualTo(Map.of(
            "v1", "some string with v1. this will not be escaped",
            "v2", "This should be escaped $${v1} $${v2}",
            "v3", "$${v3}",
            "v4", "whatever some new var ${v5}"
        ));

    // already escaped shouldn't be escaped again
    assertThat(escapeRecursiveVariables(Map.of(
        "v1", "some string with v1. this will not be escaped",
        "v2", "This should be escaped $${v1} ${v2}"
    )))
        .isEqualTo(Map.of(
            "v1", "some string with v1. this will not be escaped",
            "v2", "This should be escaped $${v1} $${v2}"
        ));

  }
}
