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
package ai.startree.thirdeye.spi.datalayer.dto;

public class AlertAssociationDto {

  private AlertDTO alert;
  private EnumerationItemDTO enumerationItem;

  public AlertDTO getAlert() {
    return alert;
  }

  public AlertAssociationDto setAlert(final AlertDTO alert) {
    this.alert = alert;
    return this;
  }

  public EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }

  public AlertAssociationDto setEnumerationItem(
      final EnumerationItemDTO enumerationItem) {
    this.enumerationItem = enumerationItem;
    return this;
  }
}
