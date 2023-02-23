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
package ai.startree.thirdeye.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

public class StringTemplateUtilsTest {

  private static String templateVariableOf(String key) {
    return "${" + key + "}";
  }

  @Test
  public void testStringReplacement() throws IOException, ClassNotFoundException {
    final Map<String, Object> values = Map.of("k1", "v1", "k2", "v2");
    final Map<String, String> map1 = StringTemplateUtils.applyContext(
        new HashMap<>(Map.of("k", "${k1}")),
        values);
    assertThat(map1).isEqualTo(Map.of("k", "v1"));
  }

  @Test
  public void testStringReplacementWithBackSlash() throws IOException, ClassNotFoundException {
    final Map<String, Object> values = Map.of("k1", "v1", "k2", "v2");
    final Map<String, String> map1 = StringTemplateUtils.applyContext(
        new HashMap<>(Map.of("k\\testBackslash", "\\withBackSlashes\\${k1}")),
        values);
    assertThat(map1).isEqualTo(Map.of("k\\testBackslash", "\\withBackSlashes\\v1"));
  }

  @Test
  public void testFailAtMissingValue() {
    final Map<String, Object> values = Map.of("k2", "v2");
    assertThatThrownBy(() -> StringTemplateUtils.applyContext(
        new HashMap<>(Map.of("k", "${k1}")),
        values)).isInstanceOf(ThirdEyeException.class);
  }

  @Test
  public void testTemplatableFieldReplacement() throws IOException, ClassNotFoundException {
    // check that the replacement is done correctly with Templatable<T>, for different Ts
    final String datasetKey = "datasetDto";
    final String mapKey = "map";
    final String listKey = "list";

    final ObjectWithTemplatableFields input = new ObjectWithTemplatableFields();
    input.templatableDto = new Templatable<DatasetConfigDTO>().setTemplatedValue(
        templateVariableOf(datasetKey));
    input.templatableMap = new Templatable<Map<String, String>>().setTemplatedValue(
        templateVariableOf(mapKey));
    input.templatableList = new Templatable<List<String>>().setTemplatedValue(
        templateVariableOf(listKey));

    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setCompletenessDelay("P7D");
    final Map<String, String> map = Map.of("test", "test2");
    final List<String> list = List.of("test");
    final Map<String, Object> properties = Map.of(datasetKey,
        datasetConfigDTO,
        mapKey,
        map,
        listKey, list);
    final ObjectWithTemplatableFields output = StringTemplateUtils.applyContext(
        input,
        properties);

    assertThat(output.templatableDto.getValue()).isEqualTo(datasetConfigDTO);
    assertThat(output.templatableDto.getTemplatedValue()).isNull();

    assertThat(output.templatableMap.getValue()).isEqualTo(map);
    assertThat(output.templatableMap.getTemplatedValue()).isNull();

    assertThat(output.templatableList.getValue()).isEqualTo(list);
    assertThat(output.templatableList.getTemplatedValue()).isNull();
  }

  @Test
  public void testTemplatableReplacementValueAlreadySet()
      throws IOException, ClassNotFoundException {
    // check that the replacement does not break when a Templatable<T> has its value NOT templated
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setCompletenessDelay("P7D");

    final ObjectWithTemplatableFields input = new ObjectWithTemplatableFields();
    input.templatableDto = Templatable.of(datasetConfigDTO);

    final Map<String, Object> properties = Map.of();
    final ObjectWithTemplatableFields output = StringTemplateUtils.applyContext(
        input,
        properties);

    assertThat(output.templatableDto.getValue()).isEqualTo(datasetConfigDTO);
    assertThat(output.templatableDto.getTemplatedValue()).isNull();
  }

  @Test
  public void testTemplatableReplacementErrorWhenKeyIsMissingInProperty() {
    final String datasetKey = "datasetDto";

    final ObjectWithTemplatableFields input = new ObjectWithTemplatableFields();
    input.templatableDto = new Templatable<DatasetConfigDTO>().setTemplatedValue(templateVariableOf(
        datasetKey));

    // datasetKey is missing
    final Map<String, Object> properties = Map.of();

    assertThatThrownBy(() -> StringTemplateUtils.applyContext(input, properties)).isInstanceOf(
        JsonMappingException.class);
  }

  @Test
  public void testNestedTemplatableReplacementWithValuesAlreadySet()
      throws IOException, ClassNotFoundException {

    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setCompletenessDelay("P7D");

    final ObjectWithTemplatableFields objectWithTemplatableFields = new ObjectWithTemplatableFields();
    objectWithTemplatableFields.templatableDto = Templatable.of(datasetConfigDTO);

    final ObjectWithNestedTemplatable objectWithNestedTemplatable = new ObjectWithNestedTemplatable();
    objectWithNestedTemplatable.setTemplatableNested(Templatable.of(objectWithTemplatableFields));

    final Map<String, Object> properties = Map.of();
    final ObjectWithNestedTemplatable output = StringTemplateUtils.applyContext(
        objectWithNestedTemplatable,
        properties);

    assertThat(output.templatableNested.getValue()).isEqualTo(objectWithTemplatableFields);
    assertThat(output.templatableNested.getTemplatedValue()).isNull();
  }

  @Test
  public void testNestedTemplatableReplacementWithHigherTemplatableTemplated()
      throws IOException, ClassNotFoundException {
    final ObjectWithNestedTemplatable objectWithNestedTemplatable = new ObjectWithNestedTemplatable();
    final String templatedValueKey = "var";
    objectWithNestedTemplatable.setTemplatableNested(new Templatable<ObjectWithTemplatableFields>().setTemplatedValue(
        templateVariableOf(templatedValueKey)));

    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setCompletenessDelay("P7D");
    final Map<String, Object> properties = Map.of(templatedValueKey,
        Map.of("templatableDto", Templatable.of(datasetConfigDTO)));
    final ObjectWithNestedTemplatable output = StringTemplateUtils.applyContext(
        objectWithNestedTemplatable,
        properties);

    //build expected object
    final ObjectWithTemplatableFields objectWithTemplatableFields = new ObjectWithTemplatableFields();
    objectWithTemplatableFields.templatableDto = Templatable.of(datasetConfigDTO);

    assertThat(output.templatableNested.getValue()).isEqualTo(objectWithTemplatableFields);
    assertThat(output.templatableNested.getTemplatedValue()).isNull();
  }

  @Test
  public void testNestedTemplatableReplacementWithNestedTemplatableTemplated()
      throws IOException, ClassNotFoundException {
    final String templatedValueKey = "var";
    final ObjectWithTemplatableFields objectWithTemplatableFields = new ObjectWithTemplatableFields().setTemplatableDto(
        new Templatable<DatasetConfigDTO>().setTemplatedValue(
            templateVariableOf(templatedValueKey)));
    ObjectWithNestedTemplatable objectWithNestedTemplatable = new ObjectWithNestedTemplatable();
    objectWithNestedTemplatable.setTemplatableNested(Templatable.of(objectWithTemplatableFields));

    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setCompletenessDelay("P7D");
    final Map<String, Object> properties = Map.of(templatedValueKey, datasetConfigDTO);
    final ObjectWithNestedTemplatable output = StringTemplateUtils.applyContext(
        objectWithNestedTemplatable,
        properties);

    assertThat(output.templatableNested.getValue().templatableDto.getValue()).isEqualTo(datasetConfigDTO);
    assertThat(output.templatableNested.getValue().templatableDto.getTemplatedValue()).isNull();
  }

  @Test
  public void testTemplateRenderingWithRecursiveVariablesForApacheCommons() throws IOException {
    final String alertTemplateDtoString = IOUtils.resourceToString("/alertTemplateDto.json",
        StandardCharsets.UTF_8);

    final String alertTemplateDtoRenderedString = IOUtils.resourceToString(
        "/alertTemplateDtoRendered.json",
        StandardCharsets.UTF_8);

    final String s = StringTemplateUtils.renderTemplate(alertTemplateDtoString,
        ImmutableMap.<String, Object>builder()
            .put("aggregationColumn", "views")
            .put("completenessDelay", "P0D")
            .put("monitoringGranularity", "P1D")
            .put("max", "${max}")
            .put("timezone", "UTC")
            .put("queryFilters", "")
            .put("aggregationFunction", "sum")
            .put("mergeMaxDuration", "")
            .put("rcaExcludedDimensions", List.of())
            .put("timeColumnFormat", "1,DAYS,SIMPLE_DATE_FORMAT,yyyyMMdd")
            .put("timeColumn", "date")
            .put("min", "${min}")
            .put("rcaAggregationFunction", "")
            .put("queryLimit", "100000000")
            .put("startTime", 1)
            .put("endTime", 2)
            .put("dataSource", "pinotQuickStartLocal")
            .put("dataset", "pageviews")
            .put("mergeMaxGap", "")
            .build());

    assertThat(s).isEqualTo(alertTemplateDtoRenderedString);
  }

  private static class ObjectWithTemplatableFields {
    // getter and setters are used by jackson

    private Templatable<List<String>> templatableList;
    private Templatable<Map<String, String>> templatableMap;
    private Templatable<DatasetConfigDTO> templatableDto;

    public Templatable<List<String>> getTemplatableList() {
      return templatableList;
    }

    public ObjectWithTemplatableFields setTemplatableList(
        final Templatable<List<String>> templatableList) {
      this.templatableList = templatableList;
      return this;
    }

    public Templatable<Map<String, String>> getTemplatableMap() {
      return templatableMap;
    }

    public ObjectWithTemplatableFields setTemplatableMap(
        final Templatable<Map<String, String>> templatableMap) {
      this.templatableMap = templatableMap;
      return this;
    }

    public Templatable<DatasetConfigDTO> getTemplatableDto() {
      return templatableDto;
    }

    public ObjectWithTemplatableFields setTemplatableDto(
        final Templatable<DatasetConfigDTO> templatableDto) {
      this.templatableDto = templatableDto;
      return this;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final ObjectWithTemplatableFields that = (ObjectWithTemplatableFields) o;
      return Objects.equals(templatableList, that.templatableList)
          && Objects.equals(templatableMap, that.templatableMap) && Objects.equals(
          templatableDto,
          that.templatableDto);
    }

    @Override
    public int hashCode() {
      return Objects.hash(templatableList, templatableMap, templatableDto);
    }
  }

  private static class ObjectWithNestedTemplatable {

    private Templatable<ObjectWithTemplatableFields> templatableNested;

    public Templatable<ObjectWithTemplatableFields> getTemplatableNested() {
      return templatableNested;
    }

    public ObjectWithNestedTemplatable setTemplatableNested(
        final Templatable<ObjectWithTemplatableFields> templatableNested) {
      this.templatableNested = templatableNested;
      return this;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final ObjectWithNestedTemplatable that = (ObjectWithNestedTemplatable) o;
      return Objects.equals(templatableNested, that.templatableNested);
    }

    @Override
    public int hashCode() {
      return Objects.hash(templatableNested);
    }
  }
}
