/*
 * Copyright 2022 StarTree Inc
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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class NotificationServiceRegistry {

  private final Map<String, NotificationServiceFactory> factoryMap = new HashMap<>();

  public void addNotificationServiceFactory(NotificationServiceFactory f) {
    checkState(!factoryMap.containsKey(f.name()),
        "Duplicate NotificationServiceFactory: " + f.name());

    factoryMap.put(f.name(), f);
  }

  public NotificationService get(
      final String name,
      final Map<String, Object> params) {
    requireNonNull(name, "name is null");
    final NotificationServiceFactory notificationServiceFactory = requireNonNull(factoryMap.get(name),
        "Unable to load NotificationServiceFactory: " + name);
    return notificationServiceFactory.build(params);
  }
}
