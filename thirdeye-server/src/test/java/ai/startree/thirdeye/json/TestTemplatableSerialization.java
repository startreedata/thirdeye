package ai.startree.thirdeye.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import org.testng.annotations.Test;

public class TestTemplatableSerialization {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(
      ApiTemplatableDeserializer.MODULE).registerModule(ApiTemplatableSerializer.MODULE);
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

    final ObjectWithTemplatable output = OBJECT_MAPPER.readValue(jsonString, ObjectWithTemplatable.class);

    final Templatable<List<String>> templatable = new Templatable<List<String>>().setTemplatedValue(variableProperty);
    final ObjectWithTemplatable expected = new ObjectWithTemplatable().setListOfStrings(templatable);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testTemplatableDeserizalizationOfStringVariableThrowsWhenStringIsInvalid() {
    final String variableProperty = "NOT_VALID${list}";
    final String jsonString = String.format("{\"listOfStrings\":\"%s\"}", variableProperty);

    assertThatThrownBy(() -> OBJECT_MAPPER.readValue(jsonString, ObjectWithTemplatable.class)).isInstanceOf(
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
    final Templatable<List<String>> templatable = new Templatable<List<String>>().setValue(value);
    final ObjectWithTemplatable obj = new ObjectWithTemplatable().setListOfStrings(templatable);

    final String output = OBJECT_MAPPER.writeValueAsString(obj);
    assertThat(output).isEqualTo(String.format("{\"listOfStrings\":[\"%s\"]}", listElement));
  }

  @Test
  public void testTemplatableDeserizalizationOfWrappedObject() throws JsonProcessingException {
    final String listElement = "dim1";
    final List<String> value = List.of(listElement);
    final String jsonString = String.format("{\"listOfStrings\":[\"%s\"]}", listElement);

    final ObjectWithTemplatable output = OBJECT_MAPPER.readValue(jsonString, ObjectWithTemplatable.class);

    final Templatable<List<String>> templatable = new Templatable<List<String>>().setValue(value);
    final ObjectWithTemplatable expected = new ObjectWithTemplatable().setListOfStrings(templatable);

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
}
