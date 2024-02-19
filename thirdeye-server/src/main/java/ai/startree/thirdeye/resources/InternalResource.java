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

import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.SecurityUtils.hmacSHA512;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.notification.NotificationPayloadBuilder;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.notification.SubscriptionGroupFilter;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.worker.task.TaskContext;
import ai.startree.thirdeye.worker.task.TaskDriver;
import ai.startree.thirdeye.worker.task.TaskDriverConfiguration;
import ai.startree.thirdeye.worker.task.runner.DetectionPipelineTaskRunner;
import ai.startree.thirdeye.worker.task.runner.NotificationTaskRunner;
import io.micrometer.core.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
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
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth")
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
public class InternalResource {

  private static final Logger log = LoggerFactory.getLogger(InternalResource.class);
  private static final Package PACKAGE = InternalResource.class.getPackage();

  private final HttpDetectorResource httpDetectorResource;
  private final DatabaseAdminResource databaseAdminResource;
  private final NotificationServiceRegistry notificationServiceRegistry;
  private final NotificationTaskRunner notificationTaskRunner;
  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final TaskDriverConfiguration taskDriverConfiguration;
  private final TaskDriver taskDriver;
  private final SubscriptionGroupFilter subscriptionGroupFilter;
  private final DetectionPipelineTaskRunner detectionPipelineTaskRunner;

  @Inject
  public InternalResource(
      final HttpDetectorResource httpDetectorResource,
      final DatabaseAdminResource databaseAdminResource,
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationTaskRunner notificationTaskRunner,
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final SubscriptionGroupManager subscriptionGroupManager,
      final TaskDriverConfiguration taskDriverConfiguration,
      final TaskDriver taskDriver,
      final SubscriptionGroupFilter subscriptionGroupFilter,
      final DetectionPipelineTaskRunner detectionPipelineTaskRunner) {
    this.httpDetectorResource = httpDetectorResource;
    this.databaseAdminResource = databaseAdminResource;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.notificationTaskRunner = notificationTaskRunner;
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.taskDriverConfiguration = taskDriverConfiguration;
    this.taskDriver = taskDriver;
    this.subscriptionGroupFilter = subscriptionGroupFilter;
    this.detectionPipelineTaskRunner = detectionPipelineTaskRunner;
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
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @QueryParam("subscriptionGroupId") final Long subscriptionGroupManagerById,
      @QueryParam("reset") final Boolean reset) {
    ensureExists(subscriptionGroupManagerById, "Query parameter required: alertId !");
    final SubscriptionGroupDTO sg = subscriptionGroupManager.findById(subscriptionGroupManagerById);
    if (reset == Boolean.TRUE) {
      sg.setVectorClocks(null);
      subscriptionGroupManager.save(sg);
    }

    requireNonNull(sg, "subscription Group is null");
    final var anomalies = requireNonNull(subscriptionGroupFilter.filter(
        sg,
        System.currentTimeMillis()), "DetectionAlertFilterResult is null");

    if (anomalies.size() == 0) {
      return Response.ok("No anomalies!").build();
    }
    final var payload = notificationPayloadBuilder.buildNotificationPayload(sg, anomalies);

    final NotificationService emailNotificationService = notificationServiceRegistry.get(
        "email-smtp",
        new HashMap<>());
    final String emailHtml = emailNotificationService.toHtml(payload).toString();
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
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("notify")
  public Response notify(@Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @FormParam("subscriptionGroupId") final Long subscriptionGroupId,
      @QueryParam("reset") final Boolean reset) throws Exception {
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
      final Object payload,
      @HeaderParam("X-Signature") final String signature
  ) throws Exception {
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

  @Path("run-detection-task-locally")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  public Response runTask(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @FormParam("alertId") final Long alertId,
      @FormParam("start") Long startTime,
      @FormParam("end") Long endTime
  ) throws Exception {
    checkArgument(alertId != null && alertId >= 0);
    if (endTime == null) {
      endTime = System.currentTimeMillis();
    }
    if (startTime == null) {
      startTime = endTime - TimeUnit.MINUTES.toMillis(1);
    }

    final DetectionPipelineTaskInfo info = new DetectionPipelineTaskInfo(alertId, startTime,
        endTime);

    detectionPipelineTaskRunner.execute(info, new TaskContext());

    return Response.ok().build();
  }
}
