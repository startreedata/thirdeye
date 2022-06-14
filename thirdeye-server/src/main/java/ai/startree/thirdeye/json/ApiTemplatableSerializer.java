package ai.startree.thirdeye.json;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;

public class ApiTemplatableSerializer extends JsonSerializer<Templatable> {

  public static final Module MODULE = new SimpleModule().addSerializer(new ApiTemplatableSerializer());

  @Override
  public Class<Templatable> handledType() {
    return Templatable.class;
  }

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
