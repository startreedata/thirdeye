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

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.google.auto.service.AutoService;
import java.util.Arrays;

@AutoService(Plugin.class)
public class EmailNotificationPlugin implements Plugin {

  @Override
  public Iterable<NotificationServiceFactory> getNotificationServiceFactories() {
    return Arrays.asList(
        new EmailSmtpNotificationServiceFactory(),
        new EmailSendgridNotificationServiceFactory()
    );
  }
}
