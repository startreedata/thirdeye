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

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoader.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
      .registerModule(new JavaTimeModule())
      .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  public static <T> T readConfig(URL url, Class<T> clazz) {
    return readConfig(url, clazz, () -> OBJECT_MAPPER.readValue(url, clazz));
  }

  public static <T> T readConfig(File f, Class<T> clazz) {
    return readConfig(f, clazz, () -> OBJECT_MAPPER.readValue(f, clazz));
  }

  public static <K, T> T readConfig(K k, Class<T> clazz, SupplierWithIO<T> supplier) {
    requireNonNull(k, "source is null");
    try {
      return requireNonNull(supplier.get(), configErrorMsg(k, clazz));
    } catch (IOException e) {
      LOG.error(configErrorMsg(k, clazz), e);
      throw new RuntimeException(e);
    }
  }

  private static <T> String configErrorMsg(final Object o, final Class<T> clazz) {
    return String.format("Failed to read into pojo. source: %s, clazz: %s", o, clazz);
  }

  @FunctionalInterface
  public interface SupplierWithIO<T> {

    T get() throws IOException;
  }
}
