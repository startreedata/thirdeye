package org.apache.pinot.thirdeye.notification;

import static org.apache.pinot.thirdeye.detection.alert.scheme.NotificationScheme.PROP_TEMPLATE;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.config.UiConfiguration;
import org.apache.pinot.thirdeye.detection.alert.scheme.NotificationScheme.EmailTemplateType;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.notification.content.NotificationContent;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.EmailContentFormatter;
import org.apache.pinot.thirdeye.spi.api.EmailEntityApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.detection.alert.DetectionAlertFilterRecipients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for sending the email alerts
 */
@Singleton
public class EmailEntityBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(EmailEntityBuilder.class);

  private final List<String> emailBlacklist = Arrays.asList(
      "me@company.com",
      "cc_email@company.com");
  private final EmailContentFormatter emailContentFormatter;
  private final SmtpConfiguration smtpConfig;
  private final UiConfiguration uiConfig;
  private final EntityGroupKeyContent entityGroupKeyContent;
  private final MetricAnomaliesContent metricAnomaliesContent;

  private final List<String> adminRecipients = new ArrayList<>();
  private final List<String> emailWhitelist = new ArrayList<>();

  @Inject
  public EmailEntityBuilder(final ThirdEyeServerConfiguration configuration,
      final EntityGroupKeyContent entityGroupKeyContent,
      final MetricAnomaliesContent metricAnomaliesContent) {
    this.entityGroupKeyContent = entityGroupKeyContent;
    this.metricAnomaliesContent = metricAnomaliesContent;

    emailContentFormatter = new EmailContentFormatter();
    smtpConfig = configuration.getNotificationConfiguration().getSmtpConfiguration();
    uiConfig = configuration.getUiConfiguration();
  }

  private Set<String> retainWhitelisted(final Set<String> recipients,
      final Collection<String> emailWhitelist) {
    if (recipients != null) {
      recipients.retainAll(emailWhitelist);
    }
    return recipients;
  }

  private Set<String> removeBlacklisted(final Set<String> recipients,
      final Collection<String> emailBlacklist) {
    if (recipients != null) {
      recipients.removeAll(emailBlacklist);
    }
    return recipients;
  }

  private void configureAdminRecipients(final DetectionAlertFilterRecipients recipients) {
    if (recipients.getCc() == null) {
      recipients.setCc(new HashSet<>());
    }
    recipients.getCc().addAll(adminRecipients);
  }

  private void whitelistRecipients(final DetectionAlertFilterRecipients recipients) {
    if (recipients != null) {
      if (!emailWhitelist.isEmpty()) {
        recipients.setTo(retainWhitelisted(recipients.getTo(), emailWhitelist));
        recipients.setCc(retainWhitelisted(recipients.getCc(), emailWhitelist));
        recipients.setBcc(retainWhitelisted(recipients.getBcc(), emailWhitelist));
      }
    }
  }

  private void blacklistRecipients(final DetectionAlertFilterRecipients recipients) {
    if (recipients != null && !emailBlacklist.isEmpty()) {
      recipients.setTo(removeBlacklisted(recipients.getTo(), emailBlacklist));
      recipients.setCc(removeBlacklisted(recipients.getCc(), emailBlacklist));
      recipients.setBcc(removeBlacklisted(recipients.getBcc(), emailBlacklist));
    }
  }

  private void validateAlert(final DetectionAlertFilterRecipients recipients,
      final List<AnomalyResult> anomalies) {
    Preconditions.checkNotNull(recipients);
    Preconditions.checkNotNull(anomalies);
    if (recipients.getTo() == null || recipients.getTo().isEmpty()) {
      throw new IllegalArgumentException("Email doesn't have any valid (whitelisted) recipients.");
    }
    if (anomalies.size() == 0) {
      throw new IllegalArgumentException("Zero anomalies found");
    }
  }

  private EmailTemplateType getTemplate(final Properties properties) {
    if (properties != null && properties.containsKey(PROP_TEMPLATE)) {
      return EmailTemplateType.valueOf(properties.get(PROP_TEMPLATE).toString());
    }
    return EmailTemplateType.DEFAULT_EMAIL;
  }

  private NotificationContent getNotificationContent(
      final Properties properties) {
    final EmailTemplateType template = getTemplate(properties);
    switch (template) {
      case DEFAULT_EMAIL:
        return metricAnomaliesContent;
      case ENTITY_GROUPBY_REPORT:
        return entityGroupKeyContent;
      default:
        throw new IllegalArgumentException(String.format("Unknown email template '%s'", template));
    }
  }

  public EmailEntityApi buildEmailEntity(
      final SubscriptionGroupDTO sg,
      final List<AnomalyResult> anomalyResults) {
    final List<AnomalyResult> sortedAnomalyResults = new ArrayList<>(anomalyResults);
    sortedAnomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

    final EmailSchemeDto emailScheme = sg.getNotificationSchemes().getEmailScheme();

    if (emailScheme.getTo() == null || emailScheme.getTo().isEmpty()) {
      throw new IllegalArgumentException(
          "No email recipients found in subscription group " + sg.getId());
    }

    final Properties emailConfig = new Properties();
//    TODO accommodate all required properties in EmailSchemeDto
//    emailConfig.putAll(ConfigUtils.getMap(sg.getNotificationSchemes().getEmailScheme()));

    final DetectionAlertFilterRecipients recipients = new DetectionAlertFilterRecipients(
        emailScheme.getTo(),
        emailScheme.getCc(),
        emailScheme.getBcc());

    configureAdminRecipients(recipients);
    whitelistRecipients(recipients);
    blacklistRecipients(recipients);
    validateAlert(recipients, sortedAnomalyResults);

    final NotificationContent content = getNotificationContent(emailConfig);
    final NotificationContext notificationContext = new NotificationContext()
        .setProperties(emailConfig)
        .setUiPublicUrl(uiConfig.getExternalUrl());
    content.init(notificationContext);

    final EmailEntityApi emailEntity = emailContentFormatter.getEmailEntity(notificationContext,
        content,
        sg,
        sortedAnomalyResults);
    emailEntity.setTo(recipients);

    if (Strings.isNullOrEmpty(sg.getFrom())) {
      final String fromAddress = smtpConfig.getUser();
      if (Strings.isNullOrEmpty(fromAddress)) {
        throw new IllegalArgumentException("Invalid sender's email");
      }

      // TODO spyne Investigate and remove logic where email send is updating dto object temporarily
      sg.setFrom(fromAddress);
    }
    emailEntity.setFrom(sg.getFrom());

    return emailEntity;
  }
}
