package ai.startree.thirdeye.rca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;
import org.junit.Test;

public class RcaInfoFetcherTest {

  public static final String DIM_1 = "dim1";
  public static final String DIM_2 = "dim2";
  public static final String DIM_3 = "dim3";
  public static final String DIM_4 = "dim4";

  @Test
  public void testAddCustomFieldsWithBothIncludedAndExcludedListNotEmpty() {
    final DatasetConfigDTO incorrectMetadataConfig = new DatasetConfigDTO()
        .setDimensions(List.of(DIM_1))
        .setRcaExcludedDimensions(new Templatable<List<String>>().setValue(List.of(DIM_2)));

    assertThatThrownBy(() -> RcaInfoFetcher.addCustomFields(new DatasetConfigDTO(),incorrectMetadataConfig)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testAddCustomFieldsWithNullDoesNothing() {
    final List<String> originalDimensions = List.of(DIM_3);
    final Templatable<List<String>> originalExcludedDimensions = new Templatable<List<String>>().setValue(List.of(DIM_4));
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
    final List<String> originalDimensions = List.of(DIM_3);
    final Templatable<List<String>> originalExcludedDimensions = new Templatable<List<String>>().setValue(List.of(DIM_4));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setDimensions(originalDimensions)
        .setRcaExcludedDimensions(originalExcludedDimensions);

    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setDimensions(List.of())
        .setRcaExcludedDimensions(new Templatable<List<String>>().setValue(List.of()));

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getDimensions()).isEqualTo(originalDimensions);
    assertThat(updatedConfig.getRcaExcludedDimensions()).isEqualTo(originalExcludedDimensions);
  }

  @Test
  public void testAddCustomFieldsWithNullInTemplatableDoesNothing() {
    final Templatable<List<String>> originalExcludedDimensions = new Templatable<List<String>>().setValue(List.of(DIM_4));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(originalExcludedDimensions);

    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(new Templatable<>());

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getRcaExcludedDimensions()).isEqualTo(originalExcludedDimensions);
  }


  @Test
  public void testAddCustomFieldsWithCustomIncludedDimensionsEmptyDoesNothing() {
    final List<String> originalDimensions = List.of(DIM_3);
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setDimensions(originalDimensions);

    final List<String> newDimensions = List.of(DIM_1);
    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setDimensions(newDimensions);

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getDimensions()).isEqualTo(newDimensions);
  }

  @Test
  public void testAddCustomFieldsWithCustomExcludedDimensionsEmptyDoesNothing() {
    final Templatable<List<String>> originalExcludedDimensions = new Templatable<List<String>>().setValue(List.of(DIM_4));
    final DatasetConfigDTO updatedConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(originalExcludedDimensions);

    final Templatable<List<String>> newExcludedDimensions = new Templatable<List<String>>().setValue(List.of(DIM_2));
    final DatasetConfigDTO metadataConfig = new DatasetConfigDTO()
        .setRcaExcludedDimensions(newExcludedDimensions);

    RcaInfoFetcher.addCustomFields(updatedConfig,metadataConfig);
    assertThat(updatedConfig.getRcaExcludedDimensions()).isEqualTo(newExcludedDimensions);
  }
}
