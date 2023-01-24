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

package ai.startree.thirdeye.spi.datalayer;

import org.joda.time.Interval;

public class AnomalyFilter {

  private Interval createTimeWindow;
  private Long alertId;
  private Long enumerationItemId;

  public Interval getCreateTimeWindow() {
    return createTimeWindow;
  }

  public AnomalyFilter setCreateTimeWindow(final Interval createTimeWindow) {
    this.createTimeWindow = createTimeWindow;
    return this;
  }

  public Long getAlertId() {
    return alertId;
  }

  public AnomalyFilter setAlertId(final Long alertId) {
    this.alertId = alertId;
    return this;
  }

  public Long getEnumerationItemId() {
    return enumerationItemId;
  }

  public AnomalyFilter setEnumerationItemId(final Long enumerationItemId) {
    this.enumerationItemId = enumerationItemId;
    return this;
  }
}
