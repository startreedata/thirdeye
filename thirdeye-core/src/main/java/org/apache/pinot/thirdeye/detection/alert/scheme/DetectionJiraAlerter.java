/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.alert.scheme;

import static java.util.Objects.requireNonNull;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.commons.JiraConfiguration;
import org.apache.pinot.thirdeye.notification.commons.JiraEntity;
import org.apache.pinot.thirdeye.notification.commons.ThirdEyeJiraClient;
import org.apache.pinot.thirdeye.notification.content.NotificationContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.JiraContentFormatter;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.detection.annotation.AlertScheme;
import org.apache.pinot.thirdeye.util.ThirdeyeMetricsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for creating the jira alert tickets
 *
 * detector.yml
 * alertSchemes:
 * - type: JIRA
 * params:
 * project: THIRDEYE    # optional, default - create jira under THIRDEYE project
 * issuetype: 19        # optional, default - create a jira TASK
 * subject: METRICS     # optional, default - follows SubjectType.METRICS format
 * assignee: user       # optional, default - unassigned
 * labels:              # optional, default - thirdeye label is always appended
 * - test-label-1
 * - test-label-2
 * custom:
 * test1: value1
 * test2: value2
 */
@AlertScheme(type = "JIRA")
public class DetectionJiraAlerter extends NotificationScheme {

  public static final String PROP_JIRA_SCHEME = "jiraScheme";
  public static final int JIRA_DESCRIPTION_MAX_LENGTH = 100000;
  public static final int JIRA_ONE_LINE_COMMENT_LENGTH = 250;
  private static final Logger LOG = LoggerFactory.getLogger(DetectionJiraAlerter.class);

  private final ThirdEyeServerConfiguration teConfig;
  private final ThirdEyeJiraClient jiraClient;
  private final JiraConfiguration jiraAdminConfig;

  public DetectionJiraAlerter(final ThirdEyeServerConfiguration thirdeyeConfig,
      final ThirdEyeJiraClient jiraClient) {
    teConfig = thirdeyeConfig;

    jiraAdminConfig = thirdeyeConfig.getNotificationConfiguration().getJiraConfiguration();
    this.jiraClient = jiraClient;
  }

  private void updateJiraAlert(final Issue issue, final JiraEntity jiraEntity) {
    // Append labels - do not remove existing labels
    jiraEntity.getLabels().addAll(issue.getLabels());
    jiraEntity.setLabels(jiraEntity.getLabels().stream().distinct().collect(Collectors.toList()));

    jiraClient.reopenIssue(issue);
    jiraClient.updateIssue(issue, jiraEntity);

    try {
      // Safeguard check from ThirdEye side
      if (jiraEntity.getDescription().length() > JIRA_DESCRIPTION_MAX_LENGTH) {
        throw new RuntimeException(
            "Exceeded jira description character limit of {}" + JIRA_DESCRIPTION_MAX_LENGTH);
      }

      jiraClient.addComment(issue, jiraEntity.getDescription());
    } catch (final Exception e) {
      // Jira has a upper limit on the number of characters in description. In such cases we will only
      // share a link in the comment.
      final StringBuilder sb = new StringBuilder();
      sb.append(
          "*<Truncating details due to jira limit! Please use the below link to view all the anomalies.>*");
      sb.append(System.getProperty("line.separator"));

      // Print only the first line with the redirection link to ThirdEye
      final String desc = jiraEntity.getDescription();
      final int newLineIndex = desc.indexOf("\n");
      if (newLineIndex < 0 || newLineIndex > JIRA_ONE_LINE_COMMENT_LENGTH) {
        sb.append(desc, 0, JIRA_ONE_LINE_COMMENT_LENGTH);
        sb.append("...");
      } else {
        sb.append(desc, 0, newLineIndex);
      }

      jiraClient.addComment(issue, sb.toString());
    }
  }

  private JiraEntity buildJiraEntity(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterNotification notification,
      final Set<MergedAnomalyResultDTO> anomalies) {
    final SubscriptionGroupDTO subsetSubsConfig = notification.getSubscriptionConfig();
//    TODO refactor when jira notification is supported
//    if (subsetSubsConfig.getAlertSchemes().get(PROP_JIRA_SCHEME) == null) {
//      throw new IllegalArgumentException(
//          "Jira not configured in subscription group " + subscriptionGroup.getId());
//    }
//
//    final Properties jiraClientConfig = new Properties();
//    jiraClientConfig
//        .putAll(ConfigUtils.getMap(subsetSubsConfig.getAlertSchemes().get(PROP_JIRA_SCHEME)));

    final Properties jiraClientConfig = new Properties();
    final List<AnomalyResult> anomalyResultListOfGroup = new ArrayList<>(anomalies);
    anomalyResultListOfGroup.sort(COMPARATOR_DESC);

    final NotificationContent content = getNotificationContent(jiraClientConfig);

    return new JiraContentFormatter(jiraAdminConfig, jiraClientConfig, content, teConfig,
        subsetSubsConfig)
        .getJiraEntity(notification.getDimensionFilters(), anomalyResultListOfGroup);
  }

  private void createJiraTickets(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult results) {
    LOG.info("Preparing a jira alert for subscription group id {}", subscriptionGroup.getId());
    Preconditions.checkNotNull(results.getResult());
    for (final Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result : results
        .getResult().entrySet()) {
      try {
        final JiraEntity jiraEntity = buildJiraEntity(subscriptionGroup,
            result.getKey(),
            result.getValue());

        // Fetch the most recent reported issues within mergeGap by jira service account under the project
        final List<Issue> issues = jiraClient.getIssues(jiraEntity.getJiraProject(),
            jiraEntity.getLabels(),
            jiraAdminConfig.getUser(),
            jiraEntity.getMergeGap());
        final Optional<Issue> latestJiraIssue = issues.stream().max(
            (o1, o2) -> o2.getCreationDate().compareTo(o1.getCreationDate()));

        if (!latestJiraIssue.isPresent()) {
          // No existing ticket found. Create a new jira ticket
          final String issueKey = jiraClient.createIssue(jiraEntity);
          ThirdeyeMetricsUtil.jiraAlertsSuccessCounter.inc();
          ThirdeyeMetricsUtil.jiraAlertsNumTicketsCounter.inc();
          LOG.info("Jira created {}, anomalies reported {}", issueKey, result.getValue().size());
        } else {
          // Reopen recent existing ticket and add a comment
          updateJiraAlert(latestJiraIssue.get(), jiraEntity);
          ThirdeyeMetricsUtil.jiraAlertsSuccessCounter.inc();
          ThirdeyeMetricsUtil.jiraAlertsNumCommentsCounter.inc();
          LOG.info("Jira updated {}, anomalies reported = {}", latestJiraIssue.get().getKey(),
              result.getValue().size());
        }
      } catch (final Exception e) {
        ThirdeyeMetricsUtil.jiraAlertsFailedCounter.inc();
        super.handleAlertFailure(e);
      }
    }
  }

  @Override
  public void destroy() {
    jiraClient.close();
  }

  @Override
  public void run(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult result) throws Exception {
    requireNonNull(result);
    if (result.getAllAnomalies().size() == 0) {
      LOG.info("Zero anomalies found, skipping jira alert for {}", subscriptionGroup.getId());
      return;
    }

    createJiraTickets(subscriptionGroup, result);
  }
}
