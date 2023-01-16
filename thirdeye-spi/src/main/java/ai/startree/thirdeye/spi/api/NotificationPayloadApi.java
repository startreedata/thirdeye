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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Objects;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class NotificationPayloadApi implements ThirdEyeApi {

  private SubscriptionGroupApi subscriptionGroup;
  private List<AnomalyReportApi> anomalyReports;
  private NotificationReportApi report;

  public SubscriptionGroupApi getSubscriptionGroup() {
    return subscriptionGroup;
  }

  public NotificationPayloadApi setSubscriptionGroup(
      final SubscriptionGroupApi subscriptionGroup) {
    this.subscriptionGroup = subscriptionGroup;
    return this;
  }

  public List<AnomalyReportApi> getAnomalyReports() {
    return anomalyReports;
  }

  public NotificationPayloadApi setAnomalyReports(final List<AnomalyReportApi> anomalyReports) {
    this.anomalyReports = anomalyReports;
    return this;
  }

  public NotificationReportApi getReport() {
    return report;
  }

  public NotificationPayloadApi setReport(final NotificationReportApi report) {
    this.report = report;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NotificationPayloadApi that = (NotificationPayloadApi) o;
    return Objects.equal(subscriptionGroup, that.subscriptionGroup)
        && Objects.equal(anomalyReports, that.anomalyReports);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(subscriptionGroup, anomalyReports);
  }
}
