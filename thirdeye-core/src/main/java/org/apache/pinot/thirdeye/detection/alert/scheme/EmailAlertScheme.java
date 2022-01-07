package org.apache.pinot.thirdeye.detection.alert.scheme;

import static org.apache.pinot.thirdeye.detection.alert.scheme.NotificationScheme.PROP_TEMPLATE;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.config.UiConfiguration;
import org.apache.pinot.thirdeye.detection.alert.AlertUtils;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.scheme.NotificationScheme.EmailTemplateType;
import org.apache.pinot.thirdeye.notification.NotificationContext;
import org.apache.pinot.thirdeye.notification.commons.EmailEntity;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.notification.content.NotificationContent;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.EmailContentFormatter;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.detection.alert.DetectionAlertFilterRecipients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for sending the email alerts
 */
@Singleton
public class EmailAlertScheme {

  public static final String PROP_RECIPIENTS = "recipients";

  private static final Logger LOG = LoggerFactory.getLogger(EmailAlertScheme.class);
  private final List<String> emailBlacklist = Arrays.asList(
      "me@company.com",
      "cc_email@company.com");
  private final EmailContentFormatter emailContentFormatter;
  private final SmtpConfiguration smtpConfig;
  private final UiConfiguration uiConfig;
  private final EntityGroupKeyContent entityGroupKeyContent;
  private final MetricAnomaliesContent metricAnomaliesContent;
  private Counter emailAlertsFailedCounter;
  private Counter emailAlertsSuccessCounter;
  private List<String> adminRecipients = new ArrayList<>();
  private List<String> emailWhitelist = new ArrayList<>();

  @Inject
  public EmailAlertScheme(final ThirdEyeServerConfiguration configuration,
      final EntityGroupKeyContent entityGroupKeyContent,
      final MetricAnomaliesContent metricAnomaliesContent,
      final MetricRegistry metricRegistry) {
    this.entityGroupKeyContent = entityGroupKeyContent;
    this.metricAnomaliesContent = metricAnomaliesContent;

    emailContentFormatter = new EmailContentFormatter();
    smtpConfig = configuration.getNotificationConfiguration().getSmtpConfiguration();
    uiConfig = configuration.getUiConfiguration();

    emailAlertsFailedCounter = metricRegistry.counter("emailAlertsFailedCounter");
    emailAlertsSuccessCounter = metricRegistry.counter("emailAlertsSuccessCounter");
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

  private HtmlEmail prepareEmailContent(final SubscriptionGroupDTO subsConfig,
      final Properties emailClientConfigs,
      final List<AnomalyResult> anomalies, final DetectionAlertFilterRecipients recipients)
      throws Exception {
    configureAdminRecipients(recipients);
    whitelistRecipients(recipients);
    blacklistRecipients(recipients);
    validateAlert(recipients, anomalies);

    final NotificationContent content = getNotificationContent(emailClientConfigs);
    final NotificationContext notificationContext = new NotificationContext()
        .setProperties(emailClientConfigs)
        .setUiPublicUrl(uiConfig.getExternalUrl());
    content.init(notificationContext);

    final EmailEntity emailEntity = emailContentFormatter.getEmailEntity(notificationContext,
        content,
        subsConfig,
        anomalies);

    if (Strings.isNullOrEmpty(subsConfig.getFrom())) {
      final String fromAddress = smtpConfig.getUser();
      if (Strings.isNullOrEmpty(fromAddress)) {
        throw new IllegalArgumentException("Invalid sender's email");
      }
      subsConfig.setFrom(fromAddress);
    }

    final HtmlEmail email = emailEntity.getContent();
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

  protected HtmlEmail getHtmlContent(final EmailEntity emailEntity) {
    return emailEntity.getContent();
  }

  /**
   * Sends email according to the provided config.
   */
  private void sendEmail(final HtmlEmail email) throws EmailException {
    email.setHostName(smtpConfig.getHost());
    email.setSmtpPort(smtpConfig.getPort());
    if (smtpConfig.getUser() != null && smtpConfig.getPassword() != null) {
      email.setAuthenticator(
          new DefaultAuthenticator(smtpConfig.getUser(), smtpConfig.getPassword()));
      email.setSSLOnConnect(true);
      email.setSslSmtpPort(Integer.toString(smtpConfig.getPort()));
    }

    // This needs to be done after the configuration phase since getMailSession() creates
    // a new mail session if required.
    email.getMailSession().getProperties().put("mail.smtp.ssl.trust", smtpConfig.getHost());

    email.send();

    final int recipientCount =
        email.getToAddresses().size() + email.getCcAddresses().size() + email.getBccAddresses()
            .size();
    LOG.info("Email sent with subject '{}' to {} recipients", email.getSubject(), recipientCount);
  }

  public void buildAndSendEmails(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult results) {
    LOG.info("Preparing an email alert for subscription group id {}", subscriptionGroup.getId());
    Preconditions.checkNotNull(results.getResult());
    for (final Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result : results
        .getResult().entrySet()) {
      try {
        final SubscriptionGroupDTO subscriptionGroupDTO = result.getKey().getSubscriptionConfig();
        if (subscriptionGroupDTO.getNotificationSchemes().getEmailScheme() == null) {
          throw new IllegalArgumentException(
              "Invalid email settings in subscription group " + subscriptionGroup.getId());
        }

        final List<AnomalyResult> anomalyResults = new ArrayList<>(result.getValue());
        anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

        buildAndSendEmail(subscriptionGroupDTO, anomalyResults);
      } catch (final Exception e) {
        emailAlertsFailedCounter.inc();
        LOG.error("Skipping! Found illegal arguments while sending alert. ", e);
      }
    }
  }

  public void buildAndSendEmail(
      final SubscriptionGroupDTO sg,
      final List<AnomalyResult> anomalyResults) throws Exception {

    final Properties emailConfig = new Properties();
//    TODO accommodate all required properties in EmailSchemeDto
//    emailConfig.putAll(ConfigUtils.getMap(sg.getNotificationSchemes().getEmailScheme()));
    final EmailSchemeDto emailScheme = sg.getNotificationSchemes().getEmailScheme();

    if (emailScheme.getTo() == null || emailScheme.getTo().isEmpty()) {
      throw new IllegalArgumentException(
          "No email recipients found in subscription group " + sg.getId());
    }

    final DetectionAlertFilterRecipients recipients = new DetectionAlertFilterRecipients(
        emailScheme.getTo(),
        emailScheme.getCc(),
        emailScheme.getBcc());
    final HtmlEmail email = prepareEmailContent(sg,
        emailConfig,
        anomalyResults,
        recipients);

    sendEmail(email);
    emailAlertsSuccessCounter.inc();
  }
}
