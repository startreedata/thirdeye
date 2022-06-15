/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.notification.email;

import static ai.startree.thirdeye.plugins.notification.email.EmailContentBuilder.DEFAULT_EMAIL_TEMPLATE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NOTIFICATION_DISPATCH;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Personalization;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSendgridNotificationService implements NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(EmailSendgridNotificationService.class);
  private final EmailSendgridConfiguration configuration;

  public EmailSendgridNotificationService(final EmailSendgridConfiguration configuration) {
    requireNonNull(configuration.getApiKey(), "api key cannot be null");
    checkArgument(!configuration.getApiKey().isBlank(), "api key cannot be blank");

    this.configuration = configuration;
  }

  @Override
  public void notify(final NotificationPayloadApi api) throws ThirdEyeException {
    try {
      final EmailContent emailContent = new EmailContentBuilder().build(api);

      sendEmail(emailContent);
    } catch (final Exception e) {
      throw new ThirdEyeException(e, ERR_NOTIFICATION_DISPATCH, "sendgrid dispatch failed!");
    }
  }

  private void sendEmail(final EmailContent emailContent) throws IOException {
    final Mail mail = buildMail(emailContent);

    final Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    final SendGrid sg = new SendGrid(configuration.getApiKey());
    final Response response = sg.api(request);

    LOG.info(String.format("Sendgrid status: %d", response.getStatusCode()));
    LOG.info(response.getBody());
    LOG.info(response.getHeaders().toString());
  }

  private Mail buildMail(final EmailContent emailContent) {
    final EmailRecipientsConfiguration emailRecipients = configuration.getEmailRecipients();
    requireNonNull(emailRecipients.getTo(), "to field in email scheme is null");
    checkArgument(emailRecipients.getTo().size() > 0, "'to' field in email scheme is empty");
    requireNonNull(emailContent, "emailEntity is null");

    final Mail mail = new Mail();
    mail.setFrom(new Email(emailRecipients.getFrom()));
    mail.setSubject(emailContent.getSubject());
    mail.addPersonalization(buildPersonalization(emailRecipients));
    mail.addContent(new Content("text/html", emailContent.getHtmlBody()));
    return mail;
  }

  private Personalization buildPersonalization(final EmailRecipientsConfiguration recipients) {
    requireNonNull(recipients, "email recipients obj is null");
    final Personalization personalization = new Personalization();

    requireNonNull(recipients.getTo(), "email 'to' is null");
    checkArgument(recipients.getTo().size() > 0, "email 'to' is empty");
    recipients.getTo()
        .stream()
        .map(Email::new)
        .forEach(personalization::addTo);

    if (recipients.getCc() != null) {
      recipients.getCc()
          .stream()
          .map(Email::new)
          .forEach(personalization::addCc);
    }

    if (recipients.getBcc() != null) {
      recipients.getBcc()
          .stream()
          .map(Email::new)
          .forEach(personalization::addBcc);
    }
    return personalization;
  }

  @Override
  public Object toHtml(final NotificationPayloadApi api) {
    final EmailContentBuilder emailContentBuilder = new EmailContentBuilder();
    final Map<String, Object> emailTemplateData = emailContentBuilder.constructTemplateData(api);
    return emailContentBuilder.buildHtml(DEFAULT_EMAIL_TEMPLATE, emailTemplateData);
  }
}
