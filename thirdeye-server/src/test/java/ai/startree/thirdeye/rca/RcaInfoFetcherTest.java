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
package ai.startree.thirdeye.rca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;
import org.testng.annotations.Test;

public class RcaInfoFetcherTest {

  public static final String DIM_1 = "dim1";
  public static final String DIM_2 = "dim2";
  public static final String DIM_3 = "dim3";
  public static final String DIM_4 = "dim4";

  @Test
  public void testAddCustomFieldsWithBothIncludedAndExcludedListNotEmpty() {
    final DatasetConfigDTO incorrectMetadataConfig = new DatasetConfigDTO()
        .setDimensions(Templatable.of(List.of(DIM_1)))
        .setRcaExcludedDimensions(Templatable.of(List.of(DIM_2)));

    assertThatThrownBy(() -> RcaInfoFetcher.addCustomFields(new DatasetConfigDTO(),incorrectMetadataConfig)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testAddCustomFieldsWithNullDoesNothing() {
    final Templatable<List<String>> originalDimensions = Templatable.of(List.of(DIM_3));
    final Templatable<List<String>> originalExcludedDimensions = Templatable.of(List.of(DIM_4));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setDimensions(originalDimensions)
        .setRcaExcludedDimensions(originalExcludedDimensions);

    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setDimensions(null)
        .setRcaExcludedDimensions(null);

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getDimensions()).isEqualTo(originalDimensions);
    assertThat(updatedConfig.getRcaExcludedDimensions()).isEqualTo(originalExcludedDimensions);
  }

  @Test
  public void testAddCustomFieldsWithEmptyDoesNothing() {
    final Templatable<List<String>> originalDimensions = Templatable.of(List.of(DIM_3));
    final Templatable<List<String>> originalExcludedDimensions = Templatable.of(List.of(DIM_4));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setDimensions(originalDimensions)
        .setRcaExcludedDimensions(originalExcludedDimensions);

    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setDimensions(Templatable.of(List.of()))
        .setRcaExcludedDimensions(Templatable.of(List.of()));

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getDimensions()).isEqualTo(originalDimensions);
    assertThat(updatedConfig.getRcaExcludedDimensions()).isEqualTo(originalExcludedDimensions);
  }

  @Test
  public void testAddCustomFieldsWithNullInTemplatableDoesNothing() {
    final Templatable<List<String>> originalExcludedDimensions = Templatable.of(List.of(DIM_4));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(originalExcludedDimensions);

    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(new Templatable<>());

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getRcaExcludedDimensions()).isEqualTo(originalExcludedDimensions);
  }


  @Test
  public void testAddCustomFieldsWithCustomIncludedDimensionsEmptyDoesNothing() {
    final Templatable<List<String>> originalDimensions = Templatable.of(List.of(DIM_3));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setDimensions(originalDimensions);

    final Templatable<List<String>> newDimensions = Templatable.of(List.of(DIM_1));
    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setDimensions(newDimensions);

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getDimensions()).isEqualTo(newDimensions);
  }

  @Test
  public void testAddCustomFieldsWithCustomExcludedDimensionsEmptyDoesNothing() {
    final Templatable<List<String>> originalExcludedDimensions = Templatable.of(List.of(DIM_4));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(originalExcludedDimensions);

    final Templatable<List<String>> newExcludedDimensions = Templatable.of(List.of(DIM_2));
    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(newExcludedDimensions);

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getRcaExcludedDimensions()).isEqualTo(newExcludedDimensions);
  }
}
