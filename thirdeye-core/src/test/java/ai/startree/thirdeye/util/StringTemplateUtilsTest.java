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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

public class StringTemplateUtilsTest {

  @Test
  public void testStringReplacement() throws IOException, ClassNotFoundException {
    final Map<String, Object> values = Map.of("k1", "v1", "k2", "v2");
    final Map<String, String> map1 = StringTemplateUtils.applyContext(
        new HashMap<>(Map.of("k", "${k1}")),
        values);
    assertThat(map1).isEqualTo(Map.of("k", "v1"));
  }
}
