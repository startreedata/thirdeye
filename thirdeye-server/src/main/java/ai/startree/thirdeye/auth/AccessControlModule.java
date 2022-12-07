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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class AccessControlModule extends AbstractModule {

  public final static AccessControl alwaysAllow = (
      final ThirdEyePrincipal principal,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> true;

  public final static AccessControl alwaysDeny = (
      final ThirdEyePrincipal principal,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> false;

  @Singleton
  @Provides
  public AccessControl provideAccessControl() {
    return alwaysAllow;
  }
}
