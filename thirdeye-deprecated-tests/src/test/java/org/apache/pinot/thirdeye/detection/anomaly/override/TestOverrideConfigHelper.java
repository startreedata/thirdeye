/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.detection.anomaly.override;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestOverrideConfigHelper {

  @Test
  public void TestTargetEntityLevel() {
    OverrideConfigDTO overrideConfigDTO = new OverrideConfigDTO();

    Map<String, List<String>> overrideTarget = new HashMap<>();
    overrideTarget.put(OverrideConfigManager.TARGET_COLLECTION, Arrays.asList("collection1"));
    overrideTarget.put(OverrideConfigManager.TARGET_METRIC, Arrays.asList("metric1", "metric2"));

    overrideTarget.put(OverrideConfigManager.EXCLUDED_METRIC, Arrays.asList("metric3"));

    overrideConfigDTO.setTargetLevel(overrideTarget);

    // Test "Only include any entity whose level has collection1, metric 1, metric 2, but not
    // metric3"
    Map<String, String> entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection1", "metric1", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection1", "metric3", 1);
    Assert.assertFalse(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection11", "metric2", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection11", "metric11", 1);
    Assert.assertFalse(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    // Test "Only include any entity whose level has collection1, metric 1, metric 2"
    overrideTarget = new HashMap<>();
    overrideTarget.put(OverrideConfigManager.TARGET_COLLECTION, Arrays.asList("collection1"));
    overrideTarget.put(OverrideConfigManager.TARGET_METRIC, Arrays.asList("metric1", "metric2"));
    overrideConfigDTO.setTargetLevel(overrideTarget);

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection1", "metric1", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection1", "metric3", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection11", "metric2", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection11", "metric11", 1);
    Assert.assertFalse(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    // Test "Include everything but collection3"
    overrideTarget = new HashMap<>();
    overrideTarget.put(OverrideConfigManager.EXCLUDED_COLLECTION, Arrays.asList("collection3"));
    overrideConfigDTO.setTargetLevel(overrideTarget);

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection1", "metric1", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection1", "metric3", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection11", "metric2", 1);
    Assert.assertTrue(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));

    entityTargetLevel =
        OverrideConfigHelper.getEntityTargetLevel("collection3", "metric2", 1);
    Assert.assertFalse(OverrideConfigHelper.isEnabled(entityTargetLevel, overrideConfigDTO));
  }
}
