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

import static org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration.SMTP_CONFIG_KEY;
import static org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration.SMTP_USER_KEY;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.detection.alert.AlertUtils;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.commons.EmailEntity;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.EmailContentFormatter;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.alert.DetectionAlertFilterRecipients;
import org.apache.pinot.thirdeye.spi.detection.annotation.AlertScheme;
import org.apache.pinot.thirdeye.util.ThirdeyeMetricsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for sending the email alerts
 */
@AlertScheme(type = "EMAIL")
public class DetectionEmailAlerter extends DetectionAlertScheme {

  public static final String PROP_RECIPIENTS = "recipients";
  public static final String PROP_EMAIL_SCHEME = "emailScheme";

  private static final Logger LOG = LoggerFactory.getLogger(DetectionEmailAlerter.class);

  private static final String PROP_TO = "to";
  private static final String PROP_CC = "cc";
  private static final String PROP_BCC = "bcc";
  private static final String PROP_EMAIL_WHITELIST = "emailWhitelist";
  private static final String PROP_ADMIN_RECIPIENTS = "adminRecipients";
  private static final String PROP_FROM_ADDRESS = "fromAddress";
  private final List<String> emailBlacklist = new ArrayList<>(
      Arrays.asList("me@company.com", "cc_email@company.com"));
  private final ThirdEyeCoordinatorConfiguration teConfig;
  private final SmtpConfiguration smtpConfig;

  public DetectionEmailAlerter(final SubscriptionGroupDTO subsConfig,
      final ThirdEyeCoordinatorConfiguration thirdeyeConfig,
      final DetectionAlertFilterResult result,
      final MetricConfigManager metricConfigManager,
      final AlertManager detectionConfigManager,
      final EventManager eventManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(subsConfig,
        result,
        metricConfigManager,
        detectionConfigManager,
        eventManager,
        mergedAnomalyResultManager);
    this.teConfig = thirdeyeConfig;
    this.smtpConfig = SmtpConfiguration
        .createFromProperties(this.teConfig.getAlerterConfigurations().get(SMTP_CONFIG_KEY));
  }

  private Set<String> retainWhitelisted(Set<String> recipients, Collection<String> emailWhitelist) {
    if (recipients != null) {
      recipients.retainAll(emailWhitelist);
    }
    return recipients;
  }

  private Set<String> removeBlacklisted(Set<String> recipients, Collection<String> emailBlacklist) {
    if (recipients != null) {
      recipients.removeAll(emailBlacklist);
    }
    return recipients;
  }

  private void configureAdminRecipients(DetectionAlertFilterRecipients recipients) {
    if (recipients.getCc() == null) {
      recipients.setCc(new HashSet<>());
    }
    recipients.getCc().addAll(ConfigUtils.getList(this.teConfig.getAlerterConfigurations()
        .get(SMTP_CONFIG_KEY).get(PROP_ADMIN_RECIPIENTS)));
  }

  private void whitelistRecipients(DetectionAlertFilterRecipients recipients) {
    if (recipients != null) {
      List<String> emailWhitelist = ConfigUtils.getList(
          this.teConfig.getAlerterConfigurations().get(SMTP_CONFIG_KEY).get(PROP_EMAIL_WHITELIST));
      if (!emailWhitelist.isEmpty()) {
        recipients.setTo(retainWhitelisted(recipients.getTo(), emailWhitelist));
        recipients.setCc(retainWhitelisted(recipients.getCc(), emailWhitelist));
        recipients.setBcc(retainWhitelisted(recipients.getBcc(), emailWhitelist));
      }
    }
  }

  private void blacklistRecipients(DetectionAlertFilterRecipients recipients) {
    if (recipients != null && !emailBlacklist.isEmpty()) {
      recipients.setTo(removeBlacklisted(recipients.getTo(), emailBlacklist));
      recipients.setCc(removeBlacklisted(recipients.getCc(), emailBlacklist));
      recipients.setBcc(removeBlacklisted(recipients.getBcc(), emailBlacklist));
    }
  }

  private void validateAlert(DetectionAlertFilterRecipients recipients,
      List<AnomalyResult> anomalies) {
    Preconditions.checkNotNull(recipients);
    Preconditions.checkNotNull(anomalies);
    if (recipients.getTo() == null || recipients.getTo().isEmpty()) {
      throw new IllegalArgumentException("Email doesn't have any valid (whitelisted) recipients.");
    }
    if (anomalies.size() == 0) {
      throw new IllegalArgumentException("Zero anomalies found");
    }
  }

  private HtmlEmail prepareEmailContent(SubscriptionGroupDTO subsConfig,
      Properties emailClientConfigs,
      List<AnomalyResult> anomalies, DetectionAlertFilterRecipients recipients) throws Exception {
    configureAdminRecipients(recipients);
    whitelistRecipients(recipients);
    blacklistRecipients(recipients);
    validateAlert(recipients, anomalies);

    BaseNotificationContent content = getNotificationContent(emailClientConfigs);
    final EmailContentFormatter emailContentFormatter = new EmailContentFormatter(emailClientConfigs,
        content,
        teConfig,
        subsConfig);
    final EmailEntity emailEntity = emailContentFormatter.getEmailEntity(anomalies);

    if (Strings.isNullOrEmpty(subsConfig.getFrom())) {
      String fromAddress = MapUtils.getString(
          teConfig.getAlerterConfigurations().get(SMTP_CONFIG_KEY),
          SMTP_USER_KEY);
      if (Strings.isNullOrEmpty(fromAddress)) {
        throw new IllegalArgumentException("Invalid sender's email");
      }
      subsConfig.setFrom(fromAddress);
    }

    HtmlEmail email = emailEntity.getContent();
    email.setSubject(emailEntity.getSubject());
    email.setFrom(subsConfig.getFrom());
    email.setTo(AlertUtils.toAddress(recipients.getTo()));
    if (!CollectionUtils.isEmpty(recipients.getCc())) {
      email.setCc(AlertUtils.toAddress(recipients.getCc()));
    }
    if (!CollectionUtils.isEmpty(recipients.getBcc())) {
      email.setBcc(AlertUtils.toAddress(recipients.getBcc()));
    }

    return getHtmlContent(emailEntity);
  }

  protected HtmlEmail getHtmlContent(EmailEntity emailEntity) {
    return emailEntity.getContent();
  }

  /**
   * Sends email according to the provided config.
   */
  private void sendEmail(HtmlEmail email) throws EmailException {
    SmtpConfiguration config = this.smtpConfig;
    email.setHostName(config.getSmtpHost());
    email.setSmtpPort(config.getSmtpPort());
    if (config.getSmtpUser() != null && config.getSmtpPassword() != null) {
      email.setAuthenticator(
          new DefaultAuthenticator(config.getSmtpUser(), config.getSmtpPassword()));
      email.setSSLOnConnect(true);
      email.setSslSmtpPort(Integer.toString(config.getSmtpPort()));
    }

    // This needs to be done after the configuration phase since getMailSession() creates
    // a new mail session if required.
    email.getMailSession().getProperties().put("mail.smtp.ssl.trust", config.getSmtpHost());

    email.send();

    int recipientCount =
        email.getToAddresses().size() + email.getCcAddresses().size() + email.getBccAddresses()
            .size();
    LOG.info("Email sent with subject '{}' to {} recipients", email.getSubject(), recipientCount);
  }

  private void buildAndSendEmails(DetectionAlertFilterResult results) throws Exception {
    LOG.info("Preparing an email alert for subscription group id {}", this.subsConfig.getId());
    Preconditions.checkNotNull(results.getResult());
    for (Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result : results
        .getResult().entrySet()) {
      try {
        final SubscriptionGroupDTO subscriptionGroupDTO = result.getKey().getSubscriptionConfig();
        if (subscriptionGroupDTO.getAlertSchemes().get(PROP_EMAIL_SCHEME) == null) {
          throw new IllegalArgumentException(
              "Invalid email settings in subscription group " + this.subsConfig.getId());
        }

        final List<AnomalyResult> anomalyResults = new ArrayList<>(result.getValue());
        anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

        buildAndSendEmail(subscriptionGroupDTO, anomalyResults);
      } catch (Exception e) {
        ThirdeyeMetricsUtil.emailAlertsFailedCounter.inc();
        super.handleAlertFailure(result.getValue().size(), e);
      }
    }
  }

  public void buildAndSendEmail(
      final SubscriptionGroupDTO sg,
      final List<AnomalyResult> anomalyResults) throws Exception {

    Properties emailConfig = new Properties();
    emailConfig.putAll(ConfigUtils.getMap(sg.getAlertSchemes().get(PROP_EMAIL_SCHEME)));

    if (emailConfig.get(PROP_RECIPIENTS) == null) {
      return;
    }

    Map<String, Object> emailRecipients = ConfigUtils.getMap(emailConfig.get(PROP_RECIPIENTS));
    if (emailRecipients.get(PROP_TO) == null || ConfigUtils
        .getList(emailRecipients.get(PROP_TO)).isEmpty()) {
      throw new IllegalArgumentException(
          "No email recipients found in subscription group " + this.subsConfig.getId());
    }

    DetectionAlertFilterRecipients recipients = new DetectionAlertFilterRecipients(
        new HashSet<>(ConfigUtils.getList(emailRecipients.get(PROP_TO))),
        new HashSet<>(ConfigUtils.getList(emailRecipients.get(PROP_CC))),
        new HashSet<>(ConfigUtils.getList(emailRecipients.get(PROP_BCC))));
    final HtmlEmail email = prepareEmailContent(sg,
        emailConfig,
        anomalyResults,
        recipients);

    sendEmail(email);
    ThirdeyeMetricsUtil.emailAlertsSucesssCounter.inc();
  }

  @Override
  public void run() throws Exception {
    Preconditions.checkNotNull(result);
    if (result.getAllAnomalies().size() == 0) {
      LOG.info("Zero anomalies found, skipping email alert for {}", this.subsConfig.getId());
      return;
    }

    buildAndSendEmails(result);
  }
}
