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
package ai.startree.thirdeye.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;
import org.testng.annotations.Test;

public class RcaResourceTest {

  public static final String DIM_1 = "dim1";
  public static final String DIM_2 = "dim2";
  private static final DatasetConfigDTO DATASET_CONFIG_SIMPLE = new DatasetConfigDTO().setDimensions(
      Templatable.of(List.of(DIM_1, DIM_2)));
  private static final DatasetConfigDTO DATASET_CONFIG_WITH_EXCLUDED_DIMENSIONS = new DatasetConfigDTO().setDimensions(
          Templatable.of(List.of(DIM_1, DIM_2)))
      .setRcaExcludedDimensions(Templatable.of(List.of(DIM_1)));
  private static final DatasetConfigDTO DATASET_CONFIG_WITH_NO_DIMENSION = new DatasetConfigDTO()
      .setRcaExcludedDimensions(Templatable.of(List.of(DIM_1)));

  @Test
  public void testGetRcaDimensionsWithBothIncludedDimensionsAndExcludedDimensionsNotEmpty() {
    assertThatThrownBy(() -> RcaResource.getRcaDimensions(List.of(DIM_1),
        List.of(DIM_2), DATASET_CONFIG_SIMPLE)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testGetRcaDimensionsWithEmptyListsChangesNothing() {
    assertThat(RcaResource.getRcaDimensions(List.of(), List.of(), DATASET_CONFIG_SIMPLE)).isEqualTo(
        List.of(DIM_1, DIM_2));
    assertThat(RcaResource.getRcaDimensions(List.of(),
        List.of(),
        DATASET_CONFIG_WITH_EXCLUDED_DIMENSIONS)).isEqualTo(List.of(DIM_2));
    assertThat(RcaResource.getRcaDimensions(List.of(),
        List.of(),
        DATASET_CONFIG_WITH_NO_DIMENSION)).isEqualTo(List.of());
  }

  @Test
  public void testGetRcaDimensionsWithIncludedDimensionsTakesPrecedence() {
    assertThat(RcaResource.getRcaDimensions(List.of(DIM_1),
        List.of(),
        DATASET_CONFIG_SIMPLE)).isEqualTo(List.of(DIM_1));
    assertThat(RcaResource.getRcaDimensions(List.of(DIM_1),
        List.of(),
        DATASET_CONFIG_WITH_EXCLUDED_DIMENSIONS)).isEqualTo(List.of(DIM_1));
    assertThat(RcaResource.getRcaDimensions(List.of(DIM_1),
        List.of(),
        DATASET_CONFIG_WITH_NO_DIMENSION)).isEqualTo(List.of(DIM_1));
  }

  @Test
  public void testGetRcaDimensionsWithExcludedDimensions() {
    assertThat(RcaResource.getRcaDimensions(List.of(),
        List.of(DIM_2),
        DATASET_CONFIG_SIMPLE)).isEqualTo(List.of(DIM_1));
    assertThat(RcaResource.getRcaDimensions(List.of(),
        List.of(DIM_2),
        DATASET_CONFIG_WITH_EXCLUDED_DIMENSIONS)).isEqualTo(List.of(DIM_1));
    assertThat(RcaResource.getRcaDimensions(List.of(),
        List.of(DIM_2),
        DATASET_CONFIG_WITH_NO_DIMENSION)).isEqualTo(List.of());
  }
}
