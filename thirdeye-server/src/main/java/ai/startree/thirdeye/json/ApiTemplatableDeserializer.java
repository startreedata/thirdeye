package ai.startree.thirdeye.json;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;

/**
 * For wrapped generic type inference at runtime,
 * see https://stackoverflow.com/questions/36159677/how-to-create-a-custom-deserializer-in-jackson-for-a-generic-type
 */
public class ApiTemplatableDeserializer extends JsonDeserializer<Templatable<?>>
    implements ContextualDeserializer {

  public static final Module MODULE = new SimpleModule().addDeserializer(Templatable.class,
      new ApiTemplatableDeserializer());

  private JavaType valueType;

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
      throws JsonMappingException {
    final JavaType wrapperType = property.getType();
    final JavaType valueType = wrapperType.containedType(0);
    final ApiTemplatableDeserializer deserializer = new ApiTemplatableDeserializer();
    deserializer.valueType = valueType;
    return deserializer;
  }

  @Override
  public Templatable<?> deserialize(final JsonParser jsonParser,
      final DeserializationContext context)
      throws IOException, JsonProcessingException {
    // case value is a variable in format ${VARIABLE_NAME}
    final String textValue = jsonParser.getText();
    if (textValue.startsWith("${")) {
      final String stringValue = context.readValue(jsonParser, String.class);
      return new Templatable<>().setTemplatedValue(stringValue);
    }

    // case value is of type T in Templatable<T>
    final Templatable<?> templatable = new Templatable<>();
    templatable.setValue(context.readValue(jsonParser, valueType));
    return templatable;
  }
}
