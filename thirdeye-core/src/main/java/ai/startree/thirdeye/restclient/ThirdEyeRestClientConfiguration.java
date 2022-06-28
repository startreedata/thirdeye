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
package ai.startree.thirdeye.restclient;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

public class ThirdEyeRestClientConfiguration {

  private static final String DEFAULT_THIRDEYE_SERVICE_NAME = "thirdeye";
  private static final String DEFAULT_THIRDEYE_SERVICE_KEY = "thirdeyeServiceKey";

  private String adminUser;
  private String sessionKey;

  public String getAdminUser() {
    return optional(adminUser).orElse(DEFAULT_THIRDEYE_SERVICE_NAME);
  }

  public void setAdminUser(String adminUser) {
    this.adminUser = adminUser;
  }

  public String getSessionKey() {
    return optional(sessionKey).orElse(DEFAULT_THIRDEYE_SERVICE_KEY);
  }

  public void setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
  }
}
