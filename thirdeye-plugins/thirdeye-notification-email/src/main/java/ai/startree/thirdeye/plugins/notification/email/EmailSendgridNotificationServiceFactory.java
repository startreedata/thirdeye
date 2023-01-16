/*
 * Copyright 2023 StarTree Inc
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

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class EmailSendgridNotificationServiceFactory implements NotificationServiceFactory {

  @Override
  public String name() {
    return "email-sendgrid";
  }

  @Override
  public NotificationService build(final Map<String, Object> params) {
    final EmailSendgridConfiguration configuration = new ObjectMapper()
        .convertValue(params, EmailSendgridConfiguration.class);

    return new EmailSendgridNotificationService(configuration);
  }
}
