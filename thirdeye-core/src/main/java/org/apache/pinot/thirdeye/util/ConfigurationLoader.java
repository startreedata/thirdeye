package org.apache.pinot.thirdeye.util;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoader.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  public static <T> T readConfig(URL url, Class<T> clazz) {
    requireNonNull(url, "source is null");
    try {
      return requireNonNull(OBJECT_MAPPER.readValue(url, clazz),
          String.format("Failed to read into pojo. source: %s, clazz: %s", url, clazz));
    } catch (IOException e) {
      LOG.error("Exception in reading data sources file {}", url, e);
      throw new RuntimeException(e);
    }
  }
}
