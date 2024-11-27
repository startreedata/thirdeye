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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.ResourceUtils.ensure;
import static ai.startree.thirdeye.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.SecurityUtils.hmacSHA512;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.service.InternalService;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.worker.task.TaskDriver;
import ai.startree.thirdeye.worker.task.TaskDriverConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "zzz Internal zzz")
@SecurityRequirement(name="oauth")
@SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth"),
    @SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(name = Constants.NAMESPACE_SECURITY, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = Constants.NAMESPACE_HTTP_HEADER)
// FIXME CYRIL - does not have authorization and workspace checks everywhere because it is not exposed in production - add checks everywhere when time just in case - or remove 
public class InternalResource {

  private static final Logger log = LoggerFactory.getLogger(InternalResource.class);
  private static final Package PACKAGE = InternalResource.class.getPackage();

  private final DatabaseAdminResource databaseAdminResource;
  private final NotificationServiceRegistry notificationServiceRegistry;
  private final TaskDriverConfiguration taskDriverConfiguration;
  private final TaskDriver taskDriver;

  private final InternalService internalService;

  @Inject
  public InternalResource(
      final DatabaseAdminResource databaseAdminResource,
      final NotificationServiceRegistry notificationServiceRegistry,
      final TaskDriverConfiguration taskDriverConfiguration, final TaskDriver taskDriver,
      final InternalService internalService) {
    this.databaseAdminResource = databaseAdminResource;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.taskDriverConfiguration = taskDriverConfiguration;
    this.taskDriver = taskDriver;
    this.internalService = internalService;
  }

  @Path("db-admin")
  public DatabaseAdminResource getDatabaseAdminResource() {
    return databaseAdminResource;
  }

  @GET
  @Path("ping")
  public Response ping() {
    return Response.ok("pong").build();
  }

  @GET
  @Path("email/html")
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response generateHtmlEmail(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @QueryParam("subscriptionGroupId") final Long subscriptionGroupManagerById,
      @QueryParam("reset") final Boolean reset) {
    ensureExists(subscriptionGroupManagerById, "Query parameter required: alertId !");
    final String emailHtml = internalService.generateHtmlEmail(principal, 
        subscriptionGroupManagerById, reset);

    return Response.ok(emailHtml).build();
  }

  @GET
  @Path("package-info")
  @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
  public Response getPackageInfo(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal) {
    return Response.ok(PACKAGE).build();
  }

  @POST
  @Path("trigger/webhook")
  public Response triggerWebhook(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal) {
    final ImmutableMap<String, Object> properties = ImmutableMap.of("url",
        "http://localhost:8080/internal/webhook");
    notificationServiceRegistry.get("webhook", properties)
        .notify(new NotificationPayloadApi().setSubscriptionGroup(
            new SubscriptionGroupApi().setName("dummy")));
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("notify")
  public Response notify(@Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @FormParam("subscriptionGroupId") final Long subscriptionGroupId,
      @QueryParam("reset") final Boolean reset) throws Exception {
    ensureExists(subscriptionGroupId, "Query parameter required: alertId !");
    internalService.notify(principal, subscriptionGroupId, reset);
    return Response.ok().build();
  }

  @POST
  @Path("webhook")
  public Response webhookDummy(final Object payload,
      @HeaderParam("X-Signature") final String signature) throws Exception {
    log.info("========================= Webhook request ==============================");
    //replace it with relevant secret key acquired during subscription group creation
    final String secretKey = "secretKey";
    final String result = hmacSHA512(payload, secretKey);
    log.info("Header signature: {}", signature);
    log.info("Generated signature: {}", result);
    if (signature != null) {
      ensure(result.equals(signature), "Broken request!");
    }
    log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload));
    log.info("========================================================================");
    return Response.ok().build();
  }

  @GET
  @Path("worker/id")
  public Response workerId(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal) {
    if (taskDriverConfiguration.isEnabled()) {
      return Response.ok(taskDriver.getWorkerId()).build();
    } else {
      return Response.ok(-1).build();
    }
  }
}
