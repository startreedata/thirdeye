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
package ai.startree.thirdeye.spi.notification;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;

public interface NotificationService {

  void notify(NotificationPayloadApi api) throws ThirdEyeException;

  /**
   * Test whether the subscription group is set up correctly and is able to send out notifications
   * @throws ThirdEyeException {@link ai.startree.thirdeye.spi.ThirdEyeStatus} contains the error
   */
  default void sendTestMessage() throws ThirdEyeException {
    throw new UnsupportedOperationException("Operation not implemented");
  }

  /**
   * Currently used to debug email notification service
   *
   * TODO spyne remove API
   *
   * @param api
   * @return
   */
  @Deprecated
  default Object toHtml(NotificationPayloadApi api) {
    throw new UnsupportedOperationException();
  }
}
