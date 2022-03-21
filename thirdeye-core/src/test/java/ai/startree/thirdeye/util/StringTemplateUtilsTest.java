/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
