/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.notification;

import com.google.inject.AbstractModule;

public class ThirdEyeNotificationModule extends AbstractModule {

  private final NotificationConfiguration notificationConfiguration;

  public ThirdEyeNotificationModule(
      final NotificationConfiguration notificationConfiguration) {
    this.notificationConfiguration = notificationConfiguration;
  }

  @Override
  protected void configure() {
    bind(NotificationConfiguration.class).toInstance(notificationConfiguration);
  }
}
