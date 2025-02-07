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
package ai.startree.thirdeye;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TestNotificationServiceFactory implements NotificationServiceFactory {

  private final AtomicReference<NotificationPayloadApi> f;

  private int count = 0;

  public TestNotificationServiceFactory() {
    this.f = new AtomicReference<>();
  }

  @Override
  public String name() {
    return "integration-test";
  }

  @Override
  public NotificationService build(final Map<String, Object> params) {
    return newValue -> {
      f.set(newValue);
      count++;
    };
  }

  public int notificationSentCount() {
    return count;
  }

  public NotificationPayloadApi lastNotificationPayload() {
    return f.get();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("f", f)
        .add("count", count)
        .toString();
  }
}
