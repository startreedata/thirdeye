/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import static ai.startree.thirdeye.notification.email.EmailContentBuilder.DEFAULT_EMAIL_TEMPLATE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NOTIFICATION_DISPATCH;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.EmailRecipientsApi;
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

public class SendgridNotificationService implements NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(SendgridNotificationService.class);

  @Override
  public void notify(final NotificationPayloadApi api) throws ThirdEyeException {
    try {
      final EmailEntityApi emailEntity = new EmailContentBuilder().buildEmailEntityApi(api);

      sendEmail(emailEntity);
    } catch (final Exception e) {
      throw new ThirdEyeException(e, ERR_NOTIFICATION_DISPATCH, "sendgrid dispatch failed!");
    }
  }

  private void sendEmail(final EmailEntityApi emailEntity) throws IOException {
    final Mail mail = buildMail(emailEntity);

    final Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    final SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
    final Response response = sg.api(request);

    LOG.info(String.format("Sendgrid status: %d", response.getStatusCode()));
    LOG.info(response.getBody());
    LOG.info(response.getHeaders().toString());
  }

  private Mail buildMail(final EmailEntityApi emailEntity) {
    requireNonNull(emailEntity, "emailEntity is null");

    final Mail mail = new Mail();
    mail.setFrom(new Email(emailEntity.getFrom()));
    mail.setSubject(emailEntity.getSubject());
    mail.addPersonalization(buildPersonalization(emailEntity.getRecipients()));
    mail.addContent(new Content("text/html", emailEntity.getHtmlContent()));
    return mail;
  }

  private Personalization buildPersonalization(final EmailRecipientsApi recipients) {
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
