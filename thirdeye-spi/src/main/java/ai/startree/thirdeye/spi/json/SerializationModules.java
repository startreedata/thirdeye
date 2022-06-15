package ai.startree.thirdeye.spi.json;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

public interface SerializationModules {

  /**
   * Serialization module for Templatable class
   */
  Module TEMPLATABLE = new SimpleModule()
      .addSerializer(Templatable.class, new ApiTemplatableSerializer())
      .addDeserializer(Templatable.class, new ApiTemplatableDeserializer());
}
