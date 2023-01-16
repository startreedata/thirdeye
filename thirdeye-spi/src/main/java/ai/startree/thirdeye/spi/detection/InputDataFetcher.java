/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.detection.model.InputData;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;

/**
 * Input data fetcher interface.
 * For components to fetch the input data it need.
 *
 * Deprecated in favor of {@link DataFetcher}
 * This is used by the v2 pipeline
 */
@Deprecated
public interface InputDataFetcher {

  /**
   * fetch data for input data spec
   */
  @Deprecated
  InputData fetchData(InputDataSpec inputDataSpec);
}
