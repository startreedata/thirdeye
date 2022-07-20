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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.List;
import java.util.Map;

// fixme cyril remove this once events are copied for tests
public class TestEventManagerImpl {

  private static final String COUNTRY_DIMENSION_KEY = "country";
  private static final String US_COUNTRY_VALUE = "US";
  private static final String FR_COUNTRY_VALUE = "FR";

  private static final String ENV_DIMENSION_KEY = "environment";
  private static final String PROD_ENV_VALUE = "prod";
  private static final String DEV_ENV_VALUE = "dev";
  private static final Map<String, List<String>> DIMENSIONS = Map.of(
      COUNTRY_DIMENSION_KEY,
      List.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE),
      ENV_DIMENSION_KEY,
      List.of(PROD_ENV_VALUE));

  private static final EventDTO CHRISTMAS_EVENT = new EventDTO().setName("CHRISTMAS")
      .setTargetDimensionMap(DIMENSIONS);
  private static final EventDTO EASTER_EVENT = new EventDTO().setName("EASTER")
      .setTargetDimensionMap(DIMENSIONS);
  private static final EventDTO FR_ONLY_EVENT = new EventDTO().setName("FR_ONLY_EVENT")
      .setTargetDimensionMap(Map.of(COUNTRY_DIMENSION_KEY,
          List.of(FR_COUNTRY_VALUE),
          ENV_DIMENSION_KEY,
          List.of(PROD_ENV_VALUE)));
  private static final EventDTO DEV_ENV_ONLY_EVENT = new EventDTO().setName("DEV_ENV_ONLY_EVENT")
      .setTargetDimensionMap(Map.of(COUNTRY_DIMENSION_KEY,
          List.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE),
          ENV_DIMENSION_KEY,
          List.of(DEV_ENV_VALUE)));

  private static final List<EventDTO> EVENT_LIST = List.of(CHRISTMAS_EVENT,
      EASTER_EVENT,
      FR_ONLY_EVENT,
      DEV_ENV_ONLY_EVENT);
}
