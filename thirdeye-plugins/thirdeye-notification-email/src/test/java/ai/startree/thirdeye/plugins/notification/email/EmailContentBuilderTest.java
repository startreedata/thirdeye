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
package ai.startree.thirdeye.plugins.notification.email;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyReportApi;
import ai.startree.thirdeye.spi.api.AnomalyReportDataApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import java.util.List;
import org.testng.annotations.Test;

public class EmailContentBuilderTest {

  public static final String SUSBCRIPTION_NAME = "subscription-name";

  /**
   * This test will break if:
   * the template is changed
   * the EmailContentBuilder$build is broken.
   *
   * If this test breaks because the template is changed:
   * - add a print of the html: System.out.println(output.getHtmlBody());
   * - ensure the generated html looks good (for instance on https://codebeautify.org/htmlviewer)
   * - copy-paste it as expected value in the test
   *
   * If the test breaks because of missing properties or NPE in EmailContentBuilder$build.
   * Fix the template to be more robust, or fix the data map logic.
   * */
  @Test
  public void testEmailBuild() {
    final EmailContentBuilder emailContentBuilder = new EmailContentBuilder();
    final AnomalyApi anomaly1 = new AnomalyApi().setMetric(new MetricApi().setName("metric-name"));
    final AnomalyReportDataApi data1 = new AnomalyReportDataApi()
        .setFunction("function-1");
    final AnomalyReportApi report1 = new AnomalyReportApi()
        .setAnomaly(anomaly1)
        .setData(data1);
    final List<AnomalyReportApi> anomalyReports = List.of(report1);
    final NotificationReportApi report = new NotificationReportApi()
        .setStartTime("12345")
        .setEndTime("67890")
        .setTimeZone("UTC")
        .setDashboardHost("host.name.com")
        .setAlertConfigName("alert-name");
    final SubscriptionGroupApi subscriptionGroup = new SubscriptionGroupApi().setName(SUSBCRIPTION_NAME);
    final NotificationPayloadApi api = new NotificationPayloadApi()
        .setAnomalyReports(anomalyReports)
        .setReport(report)
        .setSubscriptionGroup(subscriptionGroup);

    final EmailContent output = emailContentBuilder.build(api);

    assertThat(output.getSubject()).isEqualTo("Thirdeye Alert : " + SUSBCRIPTION_NAME);
    assertThat(output.getHtmlBody()).isEqualTo("\n"
        + "<head>\n"
        + "  <link href=\"https://fonts.googleapis.com/css?family=Open+Sans\" rel=\"stylesheet\">\n"
        + "</head>\n"
        + "\n"
        + "<body style=\"background-color: #EDF0F3;\">\n"
        + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%; font-family: 'Proxima Nova','Arial','Helvetica Neue',Helvetica,sans-serif; font-size:14px; margin:0 auto; max-width: 50%; min-width: 700px; background-color: #F3F6F8;\">\n"
        + "  <tr>\n"
        + "    <td align=\"center\" style=\"padding: 6px 24px;\">\n"
        + "      <span style=\"color: red; font-size: 35px; vertical-align: middle;\">&nbsp;&#9888;&nbsp;</span>\n"
        + "      <span style=\"color: rgba(0,0,0,0.75); font-size: 18px; font-weight: bold; letter-spacing: 2px; vertical-align: middle;\">ACTION REQUIRED ON THIRDEYE ALERT</span>\n"
        + "    </td>\n"
        + "  </tr>\n"
        + "\n"
        + "  <tr>\n"
        + "    <td style=\"font-size: 16px; padding: 12px; background-color: #0B263F; color: #FFF; text-align: center;\">\n"
        + "        <p>\n"
        + "          We have detected <strong>an anomaly</strong> on the metric\n"
        + "        </p>\n"
        + "\n"
        + "      <p>\n"
        + "        between <strong>12345</strong> and <strong>67890</strong> (UTC)\n"
        + "      </p>\n"
        + "\n"
        + "        <p>\n"
        + "          <a style=\"padding: 6px 12px; border-radius: 2px; border: 1px solid #FFF; font-size: 16px; font-weight: bold; color: white; text-decoration: none; line-height: 32px;\" href=\"host.name.com/anomalies/null\">View anomaly on ThirdEye</a>\n"
        + "        </p>\n"
        + "    </td>\n"
        + "  </tr>\n"
        + "\n"
        + "  <tr>\n"
        + "    <td>\n"
        + "      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #E9E9E9; border-radius: 2px; width: 100%;\">\n"
        + "\n"
        + "\n"
        + "        <!-- List all the alerts -->\n"
        + "    <tr>\n"
        + "      <td style=\"border-bottom: 1px solid rgba(0,0,0,0.15); padding: 12px 24px; align:left\">\n"
        + "\n"
        + "            <p>\n"
        + "              <span style=\"color: #1D1D1D; font-size: 20px; font-weight: bold; display:inline-block; vertical-align: middle;\">Metric:&nbsp;</span>\n"
        + "              <span style=\"color: #606060; font-size: 20px; text-decoration: none; display:inline-block; vertical-align: middle; width: 70%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;\">metric-name</span>\n"
        + "            </p>\n"
        + "\n"
        + "            <!-- List down all the alerts for the given metric -->\n"
        + "\n"
        + "\n"
        + "              <!-- List all the anomalies under this detection -->\n"
        + "              <table border=\"0\" width=\"100%\" align=\"center\" style=\"width:100%; padding:0; margin:0; border-collapse: collapse;text-align:left;\">\n"
        + "              </table>\n"
        + "\n"
        + "\n"
        + "      </td>\n"
        + "    </tr>\n"
        + "\n"
        + "        <!-- Reference Links -->\n"
        + "\n"
        + "        <!-- RCA -->\n"
        + "\n"
        + "        <!-- Holidays -->\n"
        + "      </table>\n"
        + "    </td>\n"
        + "  </tr>\n"
        + "\n"
        + "  <tr>\n"
        + "    <td style=\"text-align: center; background-color: #EDF0F3; font-size: 12px; font-family:'Proxima Nova','Arial', 'Helvetica Neue',Helvetica, sans-serif; color: #737373; padding: 12px;\">\n"
        + "      <p style=\"margin-top:0;\"> You are receiving this email because you have subscribed to ThirdEye Alert Service for\n"
        + "        <strong>alert-name</strong>.</p>\n"
        + "      <p>\n"
        + "        If you have any questions regarding this report, please email\n"
        + "        <a style=\"color: #33aada;\" href=\"mailto:thirdeye-support@startree.ai\" target=\"_top\">thirdeye-support@startree.ai</a>\n"
        + "      </p>\n"
        + "    </td>\n"
        + "  </tr>\n"
        + "\n"
        + "</table>\n"
        + "</body>\n");
  }
}
