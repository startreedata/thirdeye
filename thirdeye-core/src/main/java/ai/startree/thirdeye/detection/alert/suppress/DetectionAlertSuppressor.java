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
package ai.startree.thirdeye.detection.alert.suppress;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

/**
 * The base alert suppressor whose purpose is to suppress the  actual  alert
 * and ensure no alerts/notifications are sent to the recipients. Alerts may
 * be suppressed or delayed on various occasions to cut down the alert noise
 * especially triggered during deployments, holidays, etc.
 */
public abstract class DetectionAlertSuppressor {

  protected final SubscriptionGroupDTO config;

  public DetectionAlertSuppressor(SubscriptionGroupDTO config) {
    this.config = config;
  }

  public abstract DetectionAlertFilterResult run(DetectionAlertFilterResult result)
      throws Exception;
}
