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
package ai.startree.thirdeye.spi.json;

import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.testng.annotations.Test;

public class ThirdEyeSerializationTest {

  // fixme cyril add tests here - or in the stringutils tests
  @Test
  public void test() throws JsonProcessingException {
    final TemplatableMap<String, Object> t = ThirdEyeSerialization.newObjectMapper()
        .readValue("{\"dsf\": \"${sdfsdggs}\"}", TemplatableMap.class);

    String lol = (String) t.get("dsf");
  }

}
