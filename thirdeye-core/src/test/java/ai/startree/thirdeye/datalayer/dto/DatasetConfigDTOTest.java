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
package ai.startree.thirdeye.datalayer.dto;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DatasetConfigDTOTest {

  @Test
  public void testBucketTimeGranularity() {
    DatasetConfigDTO datasetConfigDTOEmptyBucketInfo = new DatasetConfigDTO()
        .setTimeDuration(1)
        .setTimeUnit(TimeUnit.MILLISECONDS);
    Assert.assertEquals(datasetConfigDTOEmptyBucketInfo.bucketTimeGranularity(),
        new TimeGranularity(1, TimeUnit.MILLISECONDS));

    DatasetConfigDTO datasetConfigDTOEmptyBucketSize = new DatasetConfigDTO()
        .setTimeDuration(1)
        .setTimeUnit(TimeUnit.MILLISECONDS)
        .setNonAdditiveBucketUnit(TimeUnit.MINUTES);
    Assert.assertEquals(datasetConfigDTOEmptyBucketSize.bucketTimeGranularity(),
        new TimeGranularity(1, TimeUnit.MINUTES));

    DatasetConfigDTO datasetConfigDTOEmptyBucketUnit = new DatasetConfigDTO()
        .setTimeDuration(1)
        .setTimeUnit(TimeUnit.MILLISECONDS)
        .setNonAdditiveBucketSize(5);
    Assert.assertEquals(datasetConfigDTOEmptyBucketUnit.bucketTimeGranularity(),
        new TimeGranularity(5, TimeUnit.MILLISECONDS));

    DatasetConfigDTO datasetConfigDTOFullOverride = new DatasetConfigDTO()
        .setTimeDuration(1)
        .setTimeUnit(TimeUnit.MILLISECONDS)
        .setNonAdditiveBucketSize(5)
        .setNonAdditiveBucketUnit(TimeUnit.MINUTES);
    Assert.assertEquals(datasetConfigDTOFullOverride.bucketTimeGranularity(),
        new TimeGranularity(5, TimeUnit.MINUTES));
  }
}
