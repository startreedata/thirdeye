package ai.startree.thirdeye.spi.json;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

class ApiTemplatableSerializer extends JsonSerializer<Templatable> {

  @Override
  public void serialize(final Templatable templatable, final JsonGenerator jsonGenerator,
      final SerializerProvider serializerProvider) throws IOException {
    if (templatable.getTemplatedValue() != null) {
      jsonGenerator.writeString(templatable.getTemplatedValue());
    } else {
      serializerProvider.defaultSerializeValue(templatable.getValue(), jsonGenerator);
    }
  }
}
