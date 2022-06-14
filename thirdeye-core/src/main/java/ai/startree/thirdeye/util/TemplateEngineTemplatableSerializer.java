package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;

/**
 * This serializer can be used to apply template properties.
 * Do not use for API or persistence.
 */
public class TemplateEngineTemplatableSerializer extends JsonSerializer<Templatable> {

  private final Map<String, Object> valuesMap;

  public TemplateEngineTemplatableSerializer(final Map<String, Object> valuesMap) {
    this.valuesMap = valuesMap;
  }

  @Override
  public void serialize(final Templatable templatable, final JsonGenerator jsonGenerator,
      final SerializerProvider serializerProvider) throws IOException {
    final String templatedValue = templatable.getTemplatedValue();
    if (templatedValue != null) {
      final String property = templatedValue.substring(2, templatedValue.length() - 1);
      final Object value = valuesMap.get(property);
      if (value == null) {
        throw new IllegalArgumentException(String.format(
            "Property not provided for templatable value: %s",
            property));
      }
      jsonGenerator.writeObject(new Templatable<>().setValue(value));
    } else {
      // todo cyril test null
      jsonGenerator.writeStartObject();
      // fixme cyril make this more evolvable: this is the name of a field in Templatable
      jsonGenerator.writeObjectField("value",  templatable.getValue());
      jsonGenerator.writeEndObject();
    }
  }
}
