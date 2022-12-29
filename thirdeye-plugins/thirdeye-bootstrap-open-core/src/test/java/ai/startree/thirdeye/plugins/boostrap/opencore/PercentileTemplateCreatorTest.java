package ai.startree.thirdeye.plugins.boostrap.opencore;

import static ai.startree.thirdeye.spi.util.FileUtils.readJsonObject;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.plugins.bootstrap.opencore.PercentileTemplateCreator;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.testng.annotations.Test;

public class PercentileTemplateCreatorTest {

  @Test
  public void testSingleDataFetcher() throws JsonProcessingException {
    final AlertTemplateApi input = readJsonObject(fileOf("startree-mean-variance-input.json"),
        AlertTemplateApi.class);
    final AlertTemplateApi expected = readJsonObject(
        fileOf("startree-mean-variance-percentile.json"), AlertTemplateApi.class);
    final AlertTemplateApi output = PercentileTemplateCreator.createPercentileVariant(input);

    assertWellFormedPercentileVariant(expected, output);
  }

  @NonNull
  private File fileOf(final String name) {
    ClassLoader classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(name).getFile());
  }

  @Test
  public void testMultipleDataFetcher() {
    final AlertTemplateApi input = readJsonObject(fileOf("startree-absolute-rule-input.json"),
        AlertTemplateApi.class);
    final AlertTemplateApi expected = readJsonObject(
        fileOf("startree-absolute-rule-percentile.json"), AlertTemplateApi.class);
    final AlertTemplateApi output = PercentileTemplateCreator.createPercentileVariant(input);

    assertWellFormedPercentileVariant(expected, output);
  }

  @Test
  public void testSkipIfAlreadyAPercentileTemplate() {
    final AlertTemplateApi input = readJsonObject(fileOf("startree-absolute-rule-percentile.json"),
        AlertTemplateApi.class);
    final AlertTemplateApi output = PercentileTemplateCreator.createPercentileVariant(input);

    assertThat(output).isNull();

  }

  private static void assertWellFormedPercentileVariant(final AlertTemplateApi expected, final AlertTemplateApi output) {
    assertThat(output).isNotNull();
    assertThat(output.getName()).isEqualTo(expected.getName());
    assertThat(output.getDescription()).isEqualTo(expected.getDescription());
    assertThat(output.getNodes().stream().filter(n -> n.getType().equalsIgnoreCase("DataFetcher"))
        .map(n -> n.getParams().get("component.query").value())
        .map(v -> (String) v)
        .allMatch(s -> s.contains("${aggregationParameter}"))
    ).isTrue();
    assertThat(
        output.getProperties().stream().filter(p -> p.getName().equals("aggregationParameter")).count()).isEqualTo(1);
  }
}
