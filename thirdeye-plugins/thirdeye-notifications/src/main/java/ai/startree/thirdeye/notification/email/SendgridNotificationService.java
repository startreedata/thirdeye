/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import static ai.startree.thirdeye.notification.email.EmailContentBuilder.DEFAULT_EMAIL_TEMPLATE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NOTIFICATION_DISPATCH;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendgridNotificationService implements NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(SendgridNotificationService.class);

  @Override
  public void notify(final NotificationPayloadApi api) throws ThirdEyeException {
    try {
      final EmailEntityApi emailEntity = new EmailContentBuilder().buildEmailEntityApi(
          api.getSubscriptionGroup(),
          DEFAULT_EMAIL_TEMPLATE,
          api.getEmailTemplateData(),
          api.getEmailRecipients());

      sendEmail(emailEntity);
    } catch (final Exception e) {
      throw new ThirdEyeException(e, ERR_NOTIFICATION_DISPATCH, "sendgrid dispatch failed!");
    }
  }

  private void sendEmail(final EmailEntityApi emailEntity) throws IOException {
    final Email from = new Email(emailEntity.getFrom());
    final String subject = emailEntity.getSubject();
    final Email to = new Email(emailEntity.getRecipients().getTo().iterator().next());
    final Content content = new Content("text/html", emailEntity.getHtmlContent());
    final Mail mail = new Mail(from, subject, to, content);

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

  @Override
  public Object toHtml(final NotificationPayloadApi api) {
    return new EmailContentBuilder().buildHtml(
        DEFAULT_EMAIL_TEMPLATE,
        api.getEmailTemplateData());
  }
}
