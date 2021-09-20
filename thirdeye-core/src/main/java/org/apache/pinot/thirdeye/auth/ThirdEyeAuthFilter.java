/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.auth;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeAuthFilter extends AuthFilter<ThirdEyeCredentials, ThirdEyePrincipal> {

  public static final String AUTH_TOKEN_NAME = "te_auth";
  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeAuthFilter.class);
  private static final ThreadLocal<ThirdEyePrincipal> principalAuthContextThreadLocal = new ThreadLocal<>();

  private final Set<String> allowedPaths;
  private Set<String> administrators;

  public ThirdEyeAuthFilter(Authenticator<ThirdEyeCredentials, ThirdEyePrincipal> authenticator,
      Set<String> allowedPaths, List<String> administrators) {
    this.authenticator = authenticator;
    this.allowedPaths = allowedPaths;
    if (administrators != null) {
      this.administrators = new HashSet<>(administrators);
    }
  }

  public static ThirdEyePrincipal getCurrentPrincipal() {
    // TODO refactor this, use injectors
    return principalAuthContextThreadLocal.get();
  }

  private static void setCurrentPrincipal(ThirdEyePrincipal principal) {
    // TODO refactor this, use injectors
    principalAuthContextThreadLocal.set(principal);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    setCurrentPrincipal(null);

    String uriPath = requestContext.getUriInfo().getPath();
    LOG.info("Checking auth for {}", uriPath);

    final ThirdEyePrincipal principal = getPrincipal(requestContext);
    if (principal == null) {
      // not authenticated, check exceptions

      // authenticate end points should be out of auth filter
      if (uriPath.equals("auth")
          || uriPath.equals("auth/")
          || uriPath.equals("auth/authenticate")
          || uriPath.equals("auth/logout")
          // Landing page should not throw 401
          || uriPath.equals("thirdeye")
          // Let the FE handle the redirect to login page when not authenticated
          || uriPath.equals("thirdeye-admin")
          // Let detector capture the screenshot without authentication error
          || uriPath.startsWith("anomalies/search/anomalyIds")
          || uriPath.startsWith("thirdeye/email/generate/datasets")) {
        return;
      }

      for (String fragment : this.allowedPaths) {
        if (uriPath.startsWith(fragment)) {
          return;
        }
      }

      throw new WebApplicationException("Unable to validate credentials",
          Response.Status.UNAUTHORIZED);
    } else {
      if (this.administrators != null && uriPath.equals("thirdeye-admin") && (
          principal.getName() == null
              || !this.administrators.contains(principal.getName().split("@")[0]))) {
        LOG.info("Unauthorized admin access: {}", principal.getName());
        throw new WebApplicationException("Unauthorized admin access",
            Response.Status.UNAUTHORIZED);
      }
    }

    setCurrentPrincipal(principal);

    ThirdEyeCredentials credentials = new ThirdEyeCredentials();
    credentials.setPrincipal(principal.getName());
    credentials.setToken(principal.getSessionKey());

    // Trigger the parent authentication to inject the credentials into the Security Context
    if (!this.authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
      throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }
  }

  private ThirdEyePrincipal getPrincipal(ContainerRequestContext containerRequestContext) {
    return null;
  }
}
