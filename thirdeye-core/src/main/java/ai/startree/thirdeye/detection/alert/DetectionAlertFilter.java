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
package ai.startree.thirdeye.detection.alert;

import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.DataProvider;

/**
 * The Detection alert filter.
 */
public abstract class DetectionAlertFilter {

  /**
   * The Provider.
   */
  protected final DataProvider provider;
  /**
   * The Config.
   */
  protected final SubscriptionGroupDTO config;
  /**
   * The End time.
   */
  protected final long endTime;

  /**
   * Instantiates a new Detection alert filter.
   *
   * @param provider the provider
   * @param config the config
   * @param endTime the end time
   */
  public DetectionAlertFilter(DataProvider provider, SubscriptionGroupDTO config, long endTime) {
    this.provider = provider;
    this.config = config;
    this.endTime = endTime;
  }

  /**
   * Returns a detection alert filter result for the time range between {@code startTime} and {@code
   * endTime}.
   *
   * @return alert filter result
   * @throws Exception the exception
   */
  public abstract DetectionAlertFilterResult run() throws Exception;
}
