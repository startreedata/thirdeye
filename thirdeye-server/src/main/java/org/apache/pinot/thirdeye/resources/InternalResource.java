package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.SecurityUtils.hmacSHA512;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.google.inject.Inject;
import io.swagger.annotations.Api;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.detection.alert.scheme.EmailAlertScheme;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.EmailContentFormatter;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "zzz Internal zzz")
public class InternalResource {

  private static final Logger log = LoggerFactory.getLogger(InternalResource.class);
  private static final Package PACKAGE = InternalResource.class.getPackage();

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final DatabaseAdminResource databaseAdminResource;
  private final EmailAlertScheme emailAlertScheme;
  private final MetricAnomaliesContent metricAnomaliesContent;
  private final ThirdEyeServerConfiguration configuration;
  private final EmailContentFormatter emailContentFormatter;

  @Inject
  public InternalResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final DatabaseAdminResource databaseAdminResource,
      final EmailAlertScheme emailAlertScheme,
      final MetricAnomaliesContent metricAnomaliesContent,
      final ThirdEyeServerConfiguration configuration,
      final EmailContentFormatter emailContentFormatter) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.databaseAdminResource = databaseAdminResource;
    this.emailAlertScheme = emailAlertScheme;
    this.metricAnomaliesContent = metricAnomaliesContent;
    this.configuration = configuration;
    this.emailContentFormatter = emailContentFormatter;
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
  @Path("version")
  public Response getVersion() {
    return Response.ok(InternalResource.class.getPackage().getImplementationVersion()).build();
  }

  @POST
  @Path("email/send")
  public Response sendEmail(
      @FormParam("subscriptionGroupId") Long subscriptionGroupId
  ) throws Exception {

    final SubscriptionGroupDTO sg = ensureExists(subscriptionGroupManager.findById(
        subscriptionGroupId));
    final Set<MergedAnomalyResultDTO> all = new HashSet<>(mergedAnomalyResultManager.findAll());

    emailAlertScheme.buildAndSendEmail(sg, new ArrayList<>(all));
    return Response.ok().build();
  }

  @GET
  @Path("email/html")
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response generateHtmlEmail(@QueryParam("alertId") Long alertId) {
    ensureExists(alertId, "Query parameter required: alertId !");
    final Map<String, Object> templateData = buildTemplateData(alertId);
    final String templateName = EmailContentFormatter.TEMPLATE_MAP.get(
        metricAnomaliesContent.getTemplate());

    final String emailHtml = emailContentFormatter.buildHtml(templateName, templateData);
    return Response.ok(emailHtml).build();
  }

  @GET
  @Path("email/entity")
  @JacksonFeatures(serializationEnable =  { SerializationFeature.INDENT_OUTPUT })
  @Produces(MediaType.APPLICATION_JSON)
  public Response generateEmailEntity(@QueryParam("alertId") Long alertId) {
    ensureExists(alertId, "Query parameter required: alertId !");
    return Response.ok(buildTemplateData(alertId)).build();
  }

  private Map<String, Object> buildTemplateData(final Long alertId) {
    final Set<MergedAnomalyResultDTO> anomalies = new HashSet<>(
        mergedAnomalyResultManager.findByDetectionConfigId(alertId));

    final SubscriptionGroupDTO subscriptionGroup = new SubscriptionGroupDTO()
        .setName("report-generation");

    metricAnomaliesContent.init(new Properties(), configuration);
    final Map<String, Object> templateData = metricAnomaliesContent.format(
        new ArrayList<>(anomalies),
        subscriptionGroup);
    templateData.put("dashboardHost", configuration.getUiConfiguration().getExternalUrl());
    return templateData;
  }

  @GET
  @Path("package-info")
  @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
  public Response getPackageInfo() {
    return Response.ok(PACKAGE).build();
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
