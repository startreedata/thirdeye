/*
 * Copyright 2024 StarTree Inc
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
  private Boolean isIgnored;
  private Boolean hasFeedback;
  private Boolean isChild;
  /**Will match any anomaly that overlaps with the window*/
  private Interval startEndWindow;

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

  public Boolean isIgnored() {
    return isIgnored;
  }

  public AnomalyFilter setIsIgnored(final Boolean ignored) {
    this.isIgnored = ignored;
    return this;
  }

  public Boolean hasFeedback() {
    return hasFeedback;
  }

  public AnomalyFilter setHasFeedback(final Boolean hasFeedback) {
    this.hasFeedback = hasFeedback;
    return this;
  }

  public Boolean isChild() {
    return isChild;
  }

  public AnomalyFilter setIsChild(final Boolean child) {
    isChild = child;
    return this;
  }

  public Interval getStartEndWindow() {
    return startEndWindow;
  }

  public AnomalyFilter setStartEndWindow(final Interval startEndWindow) {
    this.startEndWindow = startEndWindow;
    return this;
  }
}
