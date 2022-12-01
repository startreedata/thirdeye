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
package ai.startree.thirdeye;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.authorization.AccessControlIdentifier;

public class AccessControlUtil {

  public static AccessControlIdentifier idFromApi(ThirdEyeCrudApi<?> api) {
    if (AlertApi.class.equals(api.getClass())) {
      return new AccessControlIdentifier(
          ((AlertApi) api).getName(),
          // TODO: Add a namespace field for alerts.
          AccessControlIdentifier.DefaultNamespace,
          "alert"
      );
    }

    // TODO: Add remaining resources.

    return new AccessControlIdentifier(
        api.getId().toString(),
        AccessControlIdentifier.DefaultNamespace,
        "unspecified"
    );
  }
}
