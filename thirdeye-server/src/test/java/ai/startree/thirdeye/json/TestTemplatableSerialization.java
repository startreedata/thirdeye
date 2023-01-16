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
package ai.startree.thirdeye.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import org.testng.annotations.Test;

public class TestTemplatableSerialization {

  private static final ObjectMapper OBJECT_MAPPER = ThirdEyeSerialization.getObjectMapper();
  private static final String DOUBLE_QUOTE = "\"";

  @Test
  public void testTemplatableSerizalizationOfStringVariable() throws JsonProcessingException {
    final String variableProperty = "${list}";
    final Templatable<List<String>> templatable = new Templatable<List<String>>().setTemplatedValue(
        variableProperty);
    final ObjectWithTemplatable obj = new ObjectWithTemplatable().setListOfStrings(templatable);

    final String output = OBJECT_MAPPER.writeValueAsString(obj);
    assertThat(output).isEqualTo(String.format("{\"listOfStrings\":\"%s\"}", variableProperty));
  }

  @Test
  public void testTemplatableDeserizalizationOfStringVariable() throws JsonProcessingException {
    final String variableProperty = "${list}";
    final String jsonString = String.format("{\"listOfStrings\":\"%s\"}", variableProperty);

    final ObjectWithTemplatable output = OBJECT_MAPPER.readValue(jsonString,
        ObjectWithTemplatable.class);

    final Templatable<List<String>> templatable = new Templatable<List<String>>().setTemplatedValue(
        variableProperty);
    final ObjectWithTemplatable expected = new ObjectWithTemplatable().setListOfStrings(templatable);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testTemplatableDeserizalizationOfStringVariableThrowsWhenStringIsInvalid() {
    final String variableProperty = "NOT_VALID${list}";
    final String jsonString = String.format("{\"listOfStrings\":\"%s\"}", variableProperty);

    assertThatThrownBy(() -> OBJECT_MAPPER.readValue(jsonString,
        ObjectWithTemplatable.class)).isInstanceOf(
        JsonMappingException.class);
  }

  @Test
  public void testTemplatableDeserizalizationThrowsWhenGenericWildcard()
      throws JsonProcessingException {
    final String variableProperty = "${list}";
    final String jsonString = DOUBLE_QUOTE + variableProperty + DOUBLE_QUOTE;

    assertThatThrownBy(() -> OBJECT_MAPPER.readValue(jsonString, Templatable.class)).isInstanceOf(
        IllegalArgumentException.class);
  }

  @Test
  public void testTemplatableSerizalizationOfWrappedObject() throws JsonProcessingException {
    final String listElement = "dim1";
    final List<String> value = List.of(listElement);
    final Templatable<List<String>> templatable = Templatable.of(value);
    final ObjectWithTemplatable obj = new ObjectWithTemplatable().setListOfStrings(templatable);

    final String output = OBJECT_MAPPER.writeValueAsString(obj);
    assertThat(output).isEqualTo(String.format("{\"listOfStrings\":[\"%s\"]}", listElement));
  }

  @Test
  public void testTemplatableDeserizalizationOfWrappedObject() throws JsonProcessingException {
    final String listElement = "dim1";
    final List<String> value = List.of(listElement);
    final String jsonString = String.format("{\"listOfStrings\":[\"%s\"]}", listElement);

    final ObjectWithTemplatable output = OBJECT_MAPPER.readValue(jsonString,
        ObjectWithTemplatable.class);

    final Templatable<List<String>> templatable = Templatable.of(value);
    final ObjectWithTemplatable expected = new ObjectWithTemplatable().setListOfStrings(templatable);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testSerializationOfNestedTemplatablesWithValues() throws JsonProcessingException {
    final String listElement = "dim1";
    final List<String> value = List.of(listElement);
    final Templatable<List<String>> templatable = Templatable.of(value);
    final ObjectWithTemplatable obj = new ObjectWithTemplatable().setListOfStrings(templatable);
    final ObjectWithNestedTemplatable objectWithNestedTemplatable = new ObjectWithNestedTemplatable();
    objectWithNestedTemplatable
        .setTemplatableNested(Templatable.of(obj));

    final String output = OBJECT_MAPPER.writeValueAsString(objectWithNestedTemplatable);
    assertThat(output).isEqualTo(String.format(
        "{\"templatableNested\":{\"listOfStrings\":[\"%s\"]}}",
        listElement));
  }

  @Test
  public void testDeSerializationOfNestedTemplatablesWithValues() throws JsonProcessingException {
    final String listElement = "dim1";
    final List<String> value = List.of(listElement);
    final String jsonString = String.format("{\"templatableNested\":{\"listOfStrings\":[\"%s\"]}}",
        listElement);
    final ObjectWithNestedTemplatable output = OBJECT_MAPPER.readValue(jsonString,
        ObjectWithNestedTemplatable.class);

    // build expected object
    final ObjectWithTemplatable objectWithTemplatable = new ObjectWithTemplatable().setListOfStrings(
        Templatable.of(value));
    final Templatable<ObjectWithTemplatable> templatableNested = Templatable.of(objectWithTemplatable);
    final ObjectWithNestedTemplatable expected = new ObjectWithNestedTemplatable();
    expected.setTemplatableNested(templatableNested);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testSerializationOfNestedTemplatablesWithHigherTemplatableTemplated()
      throws JsonProcessingException {
    final ObjectWithNestedTemplatable objectWithNestedTemplatable = new ObjectWithNestedTemplatable();
    objectWithNestedTemplatable
        .setTemplatableNested(new Templatable<ObjectWithTemplatable>().setTemplatedValue("${var}"));

    final String output = OBJECT_MAPPER.writeValueAsString(objectWithNestedTemplatable);
    assertThat(output).isEqualTo("{\"templatableNested\":\"${var}\"}");
  }

  @Test
  public void testDeSerializationOfNestedTemplatablesWithHigherTemplatableTemplated()
      throws JsonProcessingException {
    final String templatedValue = "${var}";
    final String jsonString = String.format("{\"templatableNested\":\"%s\"}", templatedValue);
    final ObjectWithNestedTemplatable output = OBJECT_MAPPER.readValue(jsonString,
        ObjectWithNestedTemplatable.class);

    // build expected object
    final Templatable<ObjectWithTemplatable> templatableNested = new Templatable<ObjectWithTemplatable>().setTemplatedValue(templatedValue);
    final ObjectWithNestedTemplatable expected = new ObjectWithNestedTemplatable();
    expected.setTemplatableNested(templatableNested);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testSerializationOfNestedTemplatablesWithNestedTemplatableTemplated()
      throws JsonProcessingException {
    final String templatedValue = "${var}";
    final Templatable<List<String>> templatable = new Templatable<List<String>>().setTemplatedValue(
        templatedValue);
    final ObjectWithTemplatable obj = new ObjectWithTemplatable().setListOfStrings(templatable);
    final ObjectWithNestedTemplatable objectWithNestedTemplatable = new ObjectWithNestedTemplatable();
    objectWithNestedTemplatable
        .setTemplatableNested(Templatable.of(obj));

    final String output = OBJECT_MAPPER.writeValueAsString(objectWithNestedTemplatable);
    assertThat(output).isEqualTo(String.format("{\"templatableNested\":{\"listOfStrings\":\"%s\"}}",
        templatedValue));
  }

  @Test
  public void testDeSerializationOfNestedTemplatablesWithNestedTemplatableTemplated() throws JsonProcessingException {
    final String templatedValue = "${var}";
    final String jsonString = String.format("{\"templatableNested\":{\"listOfStrings\":\"%s\"}}", templatedValue);
    final ObjectWithNestedTemplatable output = OBJECT_MAPPER.readValue(jsonString,
        ObjectWithNestedTemplatable.class);

    // build expected object
    final ObjectWithTemplatable objectWithTemplatable = new ObjectWithTemplatable().setListOfStrings(
        new Templatable<List<String>>().setTemplatedValue(templatedValue));
    final Templatable<ObjectWithTemplatable> templatableNested = Templatable.of(
        objectWithTemplatable);
    final ObjectWithNestedTemplatable expected = new ObjectWithNestedTemplatable();
    expected.setTemplatableNested(templatableNested);

    assertThat(output).isEqualTo(expected);
  }

  private static class ObjectWithTemplatable {

    Templatable<List<String>> listOfStrings;

    public Templatable<List<String>> getListOfStrings() {
      return listOfStrings;
    }

    public ObjectWithTemplatable setListOfStrings(final Templatable<List<String>> listOfStrings) {
      this.listOfStrings = listOfStrings;
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
      final ObjectWithTemplatable that = (ObjectWithTemplatable) o;
      return Objects.equals(listOfStrings, that.listOfStrings);
    }

    @Override
    public int hashCode() {
      return Objects.hash(listOfStrings);
    }
  }

  private static class ObjectWithNestedTemplatable {

    Templatable<ObjectWithTemplatable> templatableNested;

    public Templatable<ObjectWithTemplatable> getTemplatableNested() {
      return templatableNested;
    }

    public ObjectWithNestedTemplatable setTemplatableNested(
        final Templatable<ObjectWithTemplatable> templatableNested) {
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
