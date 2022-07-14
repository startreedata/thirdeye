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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    assertThat(output.templatableDto.value()).isEqualTo(datasetConfigDTO);
    assertThat(output.templatableDto.templatedValue()).isNull();

    assertThat(output.templatableMap.value()).isEqualTo(map);
    assertThat(output.templatableMap.templatedValue()).isNull();

    assertThat(output.templatableList.value()).isEqualTo(list);
    assertThat(output.templatableList.templatedValue()).isNull();
  }

  @Test
  public void testTemplatableReplacementValueAlreadySet()
      throws IOException, ClassNotFoundException {
    // check that the replacement does not break when a Templatable<T> has its value NOT templated
    final String datasetKey = "datasetDto";
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setCompletenessDelay("P7D");

    final ObjectWithTemplatableFields input = new ObjectWithTemplatableFields();
    input.templatableDto = new Templatable<DatasetConfigDTO>().setValue(datasetConfigDTO);

    final Map<String, Object> properties = Map.of(datasetKey, datasetConfigDTO);
    final ObjectWithTemplatableFields output = StringTemplateUtils.applyContext(
        input,
        properties);

    assertThat(output.templatableDto.value()).isEqualTo(datasetConfigDTO);
    assertThat(output.templatableDto.templatedValue()).isNull();
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

  private static String templateVariableOf(String key) {
    return "${" + key + "}";
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
  }
}
