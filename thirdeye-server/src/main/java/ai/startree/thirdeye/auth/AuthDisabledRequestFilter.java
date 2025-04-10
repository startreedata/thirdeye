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
package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.spi.Constants.AUTH_BEARER;
import static ai.startree.thirdeye.spi.Constants.NO_AUTH_USER;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;

@Priority(Priorities.AUTHENTICATION - 1)
public class AuthDisabledRequestFilter implements ContainerRequestFilter {

  @Override
  public void filter(final ContainerRequestContext context) throws IOException {
    context.getHeaders()
        .addFirst(HttpHeaders.AUTHORIZATION, String.format("%s %s", AUTH_BEARER, NO_AUTH_USER));
  }
}
