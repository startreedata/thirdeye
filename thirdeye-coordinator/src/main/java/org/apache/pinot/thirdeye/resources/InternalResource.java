package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.google.inject.Inject;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.detection.alert.scheme.DetectionEmailAlerter;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "zzz Internal zzz")
public class InternalResource {

  private static final Package PACKAGE = InternalResource.class.getPackage();

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final DatabaseAdminResource databaseAdminResource;
  private final DetectionEmailAlerter detectionEmailAlerter;

  @Inject
  public InternalResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final DatabaseAdminResource databaseAdminResource,
      final DetectionEmailAlerter detectionEmailAlerter) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.databaseAdminResource = databaseAdminResource;
    this.detectionEmailAlerter = detectionEmailAlerter;
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
  @Path("email")
  public Response sendEmail(
      @FormParam("subscriptionGroupId") Long subscriptionGroupId
  ) throws Exception {

    final SubscriptionGroupDTO sg = ensureExists(subscriptionGroupManager.findById(
        subscriptionGroupId));
    final Set<MergedAnomalyResultDTO> all = new HashSet<>(mergedAnomalyResultManager.findAll());

    detectionEmailAlerter.buildAndSendEmail(sg, new ArrayList<>(all));
    return Response.ok().build();
  }

  @GET
  @Path("anomaly-report")
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response generateAnomalyReport(@QueryParam("alertId") Long alertId) {
    ensureExists(alertId, "Query parameter required: alertId !");
    final Set<MergedAnomalyResultDTO> all = new HashSet<>(mergedAnomalyResultManager.findByDetectionConfigId(
        alertId));
    return Response.ok(detectionEmailAlerter.getEmailContent(new ArrayList<>(all))).build();
  }

  @GET
  @Path("package-info")
  @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
  public Response getPackageInfo() {
    return Response.ok(PACKAGE).build();
  }
}
