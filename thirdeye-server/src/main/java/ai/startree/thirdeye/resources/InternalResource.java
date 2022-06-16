/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.SecurityUtils.hmacSHA512;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.detection.alert.NotificationSchemeFactory;
import ai.startree.thirdeye.notification.NotificationPayloadBuilder;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.task.TaskDriver;
import ai.startree.thirdeye.task.TaskDriverConfiguration;
import ai.startree.thirdeye.task.runner.NotificationTaskRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.HashMap;
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
@Api(tags = "zzz Internal zzz", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
public class InternalResource {

  private static final Logger log = LoggerFactory.getLogger(InternalResource.class);
  private static final Package PACKAGE = InternalResource.class.getPackage();

  private final HttpDetectorResource httpDetectorResource;
  private final DatabaseAdminResource databaseAdminResource;
  private final NotificationServiceRegistry notificationServiceRegistry;
  private final NotificationTaskRunner notificationTaskRunner;
  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final NotificationSchemeFactory notificationSchemeFactory;
  private final TaskDriverConfiguration taskDriverConfiguration;
  private final TaskDriver taskDriver;

  @Inject
  public InternalResource(
      final HttpDetectorResource httpDetectorResource,
      final DatabaseAdminResource databaseAdminResource,
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationTaskRunner notificationTaskRunner,
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final SubscriptionGroupManager subscriptionGroupManager,
      final NotificationSchemeFactory notificationSchemeFactory,
      final TaskDriverConfiguration taskDriverConfiguration,
      final TaskDriver taskDriver) {
    this.httpDetectorResource = httpDetectorResource;
    this.databaseAdminResource = databaseAdminResource;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.notificationTaskRunner = notificationTaskRunner;
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.notificationSchemeFactory = notificationSchemeFactory;
    this.taskDriverConfiguration = taskDriverConfiguration;
    this.taskDriver = taskDriver;
  }

  @Path("http-detector")
  public HttpDetectorResource getHttpDetectorResource() {
    return httpDetectorResource;
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
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("subscriptionGroupId") Long subscriptionGroupManagerById,
      @QueryParam("reset") Boolean reset
  ) throws Exception {
    ensureExists(subscriptionGroupManagerById, "Query parameter required: alertId !");
    final SubscriptionGroupDTO sg = subscriptionGroupManager.findById(subscriptionGroupManagerById);
    if (reset == Boolean.TRUE) {
      sg.setVectorClocks(null);
      subscriptionGroupManager.save(sg);
    }

    final DetectionAlertFilterResult result = requireNonNull(notificationSchemeFactory
        .getDetectionAlertFilterResult(sg), "DetectionAlertFilterResult is null");

    if (result.getAllAnomalies().size() == 0) {
      return Response.ok("No anomalies!").build();
    }
    final NotificationPayloadApi payload = notificationPayloadBuilder.buildNotificationPayload(
        sg,
        notificationTaskRunner.getAnomalies(sg, result));

    final NotificationService emailNotificationService = notificationServiceRegistry.get(
        "email-smtp",
        new HashMap<>());
    final String emailHtml = emailNotificationService.toHtml(payload).toString();
    return Response.ok(emailHtml).build();
  }

  @GET
  @Path("package-info")
  @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
  public Response getPackageInfo(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    return Response.ok(PACKAGE).build();
  }

  @POST
  @Path("trigger/webhook")
  public Response triggerWebhook(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    final ImmutableMap<String, Object> properties = ImmutableMap.of(
        "url", "http://localhost:8080/internal/webhook"
    );
    notificationServiceRegistry
        .get("webhook", properties)
        .notify(new NotificationPayloadApi()
            .setSubscriptionGroup(new SubscriptionGroupApi()
                .setName("dummy"))
        );
    return Response.ok().build();
  }

  @POST
  @Path("notify")
  public Response notify(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("subscriptionGroupId") Long subscriptionGroupId,
      @QueryParam("reset") Boolean reset) throws Exception {
    ensureExists(subscriptionGroupId, "Query parameter required: alertId !");
    final SubscriptionGroupDTO sg = subscriptionGroupManager.findById(subscriptionGroupId);
    if (reset == Boolean.TRUE) {
      sg.setVectorClocks(null);
      subscriptionGroupManager.save(sg);
    }
    notificationTaskRunner.execute(subscriptionGroupId);
    return Response.ok().build();
  }

  @POST
  @Path("webhook")
  public Response webhookDummy(
      Object payload,
      @HeaderParam("X-Signature") String signature
  ) throws Exception {
    log.info("========================= Webhook request ==============================");
    //replace it with relevant secret key acquired during subscription group creation
    String secretKey = "secretKey";
    String result = hmacSHA512(payload, secretKey);
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
  public Response workerId(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    if (taskDriverConfiguration.isEnabled()) {
      return Response.ok(taskDriver.getWorkerId()).build();
    } else {
      return Response.ok(-1).build();
    }
  }
}
