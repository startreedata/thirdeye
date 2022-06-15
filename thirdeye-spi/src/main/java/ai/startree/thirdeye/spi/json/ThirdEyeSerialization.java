package ai.startree.thirdeye.spi.json;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ThirdEyeSerialization {

  /**
   * Serialization module for Templatable class
   */
  public static final Module TEMPLATABLE = new SimpleModule()
      .addSerializer(Templatable.class, new ApiTemplatableSerializer())
      .addDeserializer(Templatable.class, new ApiTemplatableDeserializer());


  public static ObjectMapper newObjectMapper() {
    return new ObjectMapper().registerModule(TEMPLATABLE);
  }
}
