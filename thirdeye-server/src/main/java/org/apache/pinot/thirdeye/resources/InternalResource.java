package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.SecurityUtils.hmacSHA512;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.config.UiConfiguration;
import org.apache.pinot.thirdeye.notification.NotificationContext;
import org.apache.pinot.thirdeye.notification.NotificationServiceRegistry;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.EmailContentFormatter;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;
import org.apache.pinot.thirdeye.spi.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.task.runner.NotificationTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "zzz Internal zzz", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
public class InternalResource {

  private static final Logger log = LoggerFactory.getLogger(InternalResource.class);
  private static final Package PACKAGE = InternalResource.class.getPackage();

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final DatabaseAdminResource databaseAdminResource;
  private final MetricAnomaliesContent metricAnomaliesContent;
  private final ThirdEyeServerConfiguration configuration;
  private final EmailContentFormatter emailContentFormatter;
  private final NotificationServiceRegistry notificationServiceRegistry;
  private final NotificationTaskRunner notificationTaskRunner;

  @Inject
  public InternalResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final DatabaseAdminResource databaseAdminResource,
      final MetricAnomaliesContent metricAnomaliesContent,
      final ThirdEyeServerConfiguration configuration,
      final EmailContentFormatter emailContentFormatter,
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationTaskRunner notificationTaskRunner) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.databaseAdminResource = databaseAdminResource;
    this.metricAnomaliesContent = metricAnomaliesContent;
    this.configuration = configuration;
    this.emailContentFormatter = emailContentFormatter;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.notificationTaskRunner = notificationTaskRunner;
  }

  @Path("db-admin")
  public DatabaseAdminResource getDatabaseAdminResource() {
    return databaseAdminResource;
  }

  @GET
  @Path("ping")
  public Response ping(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    return Response.ok("pong").build();
  }

  @GET
  @Path("version")
  public Response getVersion(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    return Response.ok(InternalResource.class.getPackage().getImplementationVersion()).build();
  }

  @GET
  @Path("email/html")
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response generateHtmlEmail(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("alertId") Long alertId
  ) {
    ensureExists(alertId, "Query parameter required: alertId !");
    final Map<String, Object> templateData = buildTemplateData(alertId);
    final String templateName = EmailContentFormatter.TEMPLATE_MAP.get(
        metricAnomaliesContent.getTemplate());

    final String emailHtml = emailContentFormatter.buildHtml(templateName, templateData);
    return Response.ok(emailHtml).build();
  }

  @GET
  @Path("email/entity")
  @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
  @Produces(MediaType.APPLICATION_JSON)
  public Response generateEmailEntity(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("alertId") Long alertId
  ) {
    ensureExists(alertId, "Query parameter required: alertId !");
    return Response.ok(buildTemplateData(alertId)).build();
  }

  private Map<String, Object> buildTemplateData(final Long alertId) {
    final Set<MergedAnomalyResultDTO> anomalies = new HashSet<>(
        mergedAnomalyResultManager.findByDetectionConfigId(alertId));

    final SubscriptionGroupDTO subscriptionGroup = new SubscriptionGroupDTO()
        .setName("report-generation");

    metricAnomaliesContent.init(new NotificationContext()
        .setProperties(new Properties())
        .setUiPublicUrl(optional(configuration)
            .map(ThirdEyeServerConfiguration::getUiConfiguration)
            .map(UiConfiguration::getExternalUrl)
            .orElse("")));
    final Map<String, Object> templateData = metricAnomaliesContent.format(
        new ArrayList<>(anomalies),
        subscriptionGroup);
    templateData.put("dashboardHost", configuration.getUiConfiguration().getExternalUrl());
    return templateData;
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
    final ImmutableMap<String, String> properties = ImmutableMap.of(
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
  public Response triggerWebhook(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("subscriptionGroupId") Long subscriptionGroupId) throws Exception {
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
    ensure(result.equals(signature), "Broken request!");
    log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload));
    log.info("========================================================================");
    return Response.ok().build();
  }
}
