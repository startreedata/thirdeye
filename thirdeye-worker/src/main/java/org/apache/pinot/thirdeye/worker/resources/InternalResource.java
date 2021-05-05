package org.apache.pinot.thirdeye.worker.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.config.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.scheme.DetectionEmailAlerter;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "Internal")
@Singleton
public class InternalResource {

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final EventManager eventManager;
  private final AlertManager detectionConfigManager;
  private final MetricConfigManager metricConfigManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final ThirdEyeWorkerConfiguration thirdEyeWorkerConfiguration;

  @Inject
  public InternalResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final EventManager eventManager,
      final AlertManager detectionConfigManager,
      final MetricConfigManager metricConfigManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final ThirdEyeWorkerConfiguration thirdEyeWorkerConfiguration) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.eventManager = eventManager;
    this.detectionConfigManager = detectionConfigManager;
    this.metricConfigManager = metricConfigManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.thirdEyeWorkerConfiguration = thirdEyeWorkerConfiguration;
  }

  @POST
  @Path("email")
  public Response sendEmail(
      @FormParam("subscriptionGroupId") Long subscriptionGroupId
  ) throws Exception {

    final SubscriptionGroupDTO sg = ensureExists(subscriptionGroupManager.findById(
        subscriptionGroupId));
    final Set<MergedAnomalyResultDTO> all = new HashSet<>(mergedAnomalyResultManager.findAll());
    final DetectionAlertFilterResult result = new DetectionAlertFilterResult(
        ImmutableMap.of(new DetectionAlertFilterNotification(sg), all)
    );
    DetectionEmailAlerter instance = new DetectionEmailAlerter(
        sg,
        thirdEyeWorkerConfiguration,
        result,
        metricConfigManager,
        detectionConfigManager,
        eventManager,
        mergedAnomalyResultManager);

    instance.buildAndSendEmail(sg, new ArrayList<>(all));
    return Response.ok().build();
  }

  @GET
  @Path("version")
  public Response getVersion() {
    return Response.ok(InternalResource.class.getPackage().getImplementationVersion()).build();
  }
}
