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
package ai.startree.thirdeye.plugins.notification.slack;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.context;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.AnomalyReportApi;
import ai.startree.thirdeye.spi.api.AnomalyReportDataApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.slack.api.Slack;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.webhook.Payload;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackNotificationService implements NotificationService {

  public static final int MAX_ANOMALIES_TO_REPORT = 5;
  private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationService.class);

  private final String webhookUrl;
  private final Slack slack;

  public SlackNotificationService(final SlackConfiguration slackConfiguration) {
    webhookUrl = slackConfiguration.getWebhookUrl();
    slack = Slack.getInstance();
  }

  public static void main(final String[] args) {
    final SlackNotificationService service = new SlackNotificationService(new SlackConfiguration()
        .setWebhookUrl(System.getenv("SLACK_WEBHOOK_URL")));
    service.notify(null);
  }

  private static String anomalyReportToText(final AnomalyReportApi reportApi) {
    final AnomalyReportDataApi data = reportApi.getData();
    final String msg = String.format(
        "<%s|*%s Deviation%s*> Started %s %s. Duration: %s, Value: %s Baseline: %s",
        reportApi.getUrl(),
        data.getLift(),
        optional(reportApi.getData().getMetric()).map(m -> " in " + m).orElse(""),
        data.getStartDateTime(),
        data.getTimezone(),
        data.getDuration(),
        data.getCurrentVal(),
        data.getBaselineVal()
    );
    return msg;
  }

  private static String header(final NotificationPayloadApi api) {
    final int nAnomalies = api.getAnomalyReports().size();
    return String.format("*Alert! :alert: * <%s|*%d %s*> found from *%s to %s (%s)*\n\n",
        allAnomaliesUrl(api),
        nAnomalies,
        nAnomalies == 1 ? "anomaly" : "anomalies",
        api.getReport().getStartTime(),
        api.getReport().getEndTime(),
        api.getReport().getTimeZone()
    );
  }

  private static String allAnomaliesUrl(final NotificationPayloadApi api) {
    return String.format("%s/anomalies/all", api.getReport().getDashboardHost());
  }

  private static String notificationMsg(final NotificationPayloadApi api) {
    final int nAnomalies = api.getAnomalyReports().size();
    return String.format("Alert! %d %s found from %s to %s (%s)",
        nAnomalies,
        nAnomalies == 1 ? "anomaly" : "anomalies",
        api.getReport().getStartTime(),
        api.getReport().getEndTime(),
        api.getReport().getTimeZone()
    );
  }

  @Override
  public void notify(final NotificationPayloadApi api) {
    try {
      slack.send(webhookUrl, buildPayload(api));
    } catch (final NoSuchMethodError e) {
      final String msg = "Known issue!!! If running from Debug Server, slack api client requires"
          + " com.squareup.okhttp3:okhttp:jar:4.9.3. Please add to maven dependency to fix this."
          + " This runs fine from distribution since the classpaths are separated out.";
      /*
       Possible fix: Add this to the pom
           <dependency>
             <groupId>com.squareup.okhttp3</groupId>
             <artifactId>okhttp</artifactId>
             <version>4.9.3</version>
           </dependency>
       */
      LOG.error(msg, e);
      throw new ThirdEyeException(ThirdEyeStatus.ERR_NOTIFICATION_DISPATCH,
          "Error firing Slack msg: " + msg);
    } catch (final IOException e) {
      throw new ThirdEyeException(ThirdEyeStatus.ERR_NOTIFICATION_DISPATCH,
          "Error firing Slack msg: " + e.getMessage());
    }
  }

  private Payload buildPayload(final NotificationPayloadApi api) {
    final List<String> anomalyTexts = api.getAnomalyReports()
        .stream()
        .limit(MAX_ANOMALIES_TO_REPORT)
        .map(SlackNotificationService::anomalyReportToText)
        .collect(Collectors.toList());

    final List<LayoutBlock> blocks = new ArrayList<>(asBlocks(
        section(builder -> builder.text(markdownText(header(api)))),
        divider(),
        context(builder -> builder.elements(anomalyTexts.stream()
            .map(BlockCompositions::markdownText)
            .collect(Collectors.toList())
        ))));

    /* Slack fails to show entire msg if the list is too long. */
    if (api.getAnomalyReports().size() > MAX_ANOMALIES_TO_REPORT) {
      blocks.add(section(builder -> builder.text(markdownText(
          String.format("Showing %d most recent anomalies. <%s|View All>",
              MAX_ANOMALIES_TO_REPORT,
              allAnomaliesUrl(api))
      ))));
    }
    return Payload.builder()
        .text(notificationMsg(api))
        .blocks(blocks)
        .build();
  }
}
