/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import static ai.startree.thirdeye.notification.email.EmailContentBuilder.DEFAULT_EMAIL_TEMPLATE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NOTIFICATION_DISPATCH;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.EmailRecipientsApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import javax.mail.internet.InternetAddress;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationService implements NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);
  private final SmtpConfiguration smtpConfiguration;

  public EmailNotificationService(final SmtpConfiguration configuration) {
    this.smtpConfiguration = configuration;
  }

  /**
   * Helper to convert a collection of email strings into {@code InternetAddress} instances,
   * filtering
   * out invalid addresses and nulls.
   *
   * @param emailCollection collection of email address strings
   * @return filtered collection of InternetAddress objects
   */
  public static Collection<InternetAddress> toAddress(Collection<String> emailCollection) {
    if (CollectionUtils.isEmpty(emailCollection)) {
      return Collections.emptySet();
    }
    return Collections2.filter(Collections2.transform(emailCollection,
            EmailNotificationService::toInternetAddress),
        Objects::nonNull);
  }

  private static InternetAddress toInternetAddress(final String s) {
    try {
      return new InternetAddress(s);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void notify(final NotificationPayloadApi api) throws ThirdEyeException {
    try {
      final EmailEntityApi emailEntity = new EmailContentBuilder().buildEmailEntityApi(
          api.getSubscriptionGroup(),
          DEFAULT_EMAIL_TEMPLATE,
          api.getEmailTemplateData(),
          api.getEmailRecipients());

      final HtmlEmail email = buildHtmlEmail(emailEntity);
      sendEmail(email);
    } catch (Exception e) {
      throw new ThirdEyeException(e, ERR_NOTIFICATION_DISPATCH, "Email dispatch failed!");
    }
  }

  private HtmlEmail buildHtmlEmail(final EmailEntityApi emailEntity)
      throws EmailException {
    final HtmlEmail email = new HtmlEmail();
    final EmailRecipientsApi recipients = emailEntity.getRecipients();

    email.setSubject(emailEntity.getSubject());
    email.setFrom(emailEntity.getFrom());
    email.setTo(toAddress(recipients.getTo()));
    email.setContent(emailEntity.getHtmlContent(), "text/html; charset=utf-8");

    if (!CollectionUtils.isEmpty(recipients.getCc())) {
      email.setCc(toAddress(recipients.getCc()));
    }

    if (!CollectionUtils.isEmpty(recipients.getBcc())) {
      email.setBcc(toAddress(recipients.getBcc()));
    }

    return email;
  }

  /**
   * Sends email according to the provided config.
   */
  private void sendEmail(final HtmlEmail email) throws EmailException {
    email.setHostName(smtpConfiguration.getHost());
    email.setSmtpPort(smtpConfiguration.getPort());
    if (smtpConfiguration.getUser() != null && smtpConfiguration.getPassword() != null) {
      email.setAuthenticator(
          new DefaultAuthenticator(smtpConfiguration.getUser(), smtpConfiguration.getPassword()));
      email.setSSLOnConnect(true);
      email.setSslSmtpPort(Integer.toString(smtpConfiguration.getPort()));
    }

    // This needs to be done after the configuration phase since getMailSession() creates
    // a new mail session if required.
    email.getMailSession().getProperties().put("mail.smtp.ssl.trust", smtpConfiguration.getHost());

    email.send();

    final int recipientCount =
        email.getToAddresses().size() + email.getCcAddresses().size() + email.getBccAddresses()
            .size();
    LOG.info("Email sent with subject '{}' to {} recipients", email.getSubject(), recipientCount);
  }

  @Override
  public Object toHtml(final NotificationPayloadApi api) {
    return new EmailContentBuilder().buildHtml(
        DEFAULT_EMAIL_TEMPLATE,
        api.getEmailTemplateData());
  }
}
