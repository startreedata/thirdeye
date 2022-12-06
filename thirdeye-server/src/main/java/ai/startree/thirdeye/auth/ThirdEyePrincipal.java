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
package ai.startree.thirdeye.auth;

import java.security.Principal;

public class ThirdEyePrincipal implements Principal {

  private final String name;
  public final String authToken;

  public ThirdEyePrincipal(final String name, final String authToken) {
    this.name = name;
    this.authToken = authToken;
  }

  @Override
  public String getName() {
    return name;
  }
}
