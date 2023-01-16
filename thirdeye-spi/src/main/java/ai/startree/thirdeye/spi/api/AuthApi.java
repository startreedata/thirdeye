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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AuthApi implements ThirdEyeApi {

  private UserApi user;
  private String accessToken;

  public UserApi getUser() {
    return user;
  }

  public AuthApi setUser(final UserApi user) {
    this.user = user;
    return this;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public AuthApi setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
    return this;
  }
}
