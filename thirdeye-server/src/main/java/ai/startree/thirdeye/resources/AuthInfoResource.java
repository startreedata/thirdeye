/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.ResourceUtils.respondOk;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.OAuthManager;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Auth Info")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AuthInfoResource {

  private final OAuthManager oAuthManager;
  private final AuthConfiguration authConfig;

  @Inject
  public AuthInfoResource(final OAuthManager oAuthManager, final AuthConfiguration authConfig) {
    this.oAuthManager = oAuthManager;
    this.authConfig = authConfig;
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    final AuthInfoApi info = oAuthManager.getInfo();
    if (authConfig.isEnabled() && info == null) {
      throw serverError(ThirdEyeStatus.ERR_AUTH_SERVER_NOT_RESPONDING, authConfig.getOAuthConfig().getServerUrl());
    }
    return respondOk(info);
  }
}
