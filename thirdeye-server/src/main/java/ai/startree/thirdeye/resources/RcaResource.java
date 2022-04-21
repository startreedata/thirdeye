/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.rca.RootCauseAnalysisService;
import ai.startree.thirdeye.rca.RootCauseEntityFormatter;
import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.RCAFramework;
import ai.startree.thirdeye.rootcause.RCAFrameworkExecutionResult;
import ai.startree.thirdeye.rootcause.entity.TimeRangeEntity;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.RootCauseEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaResource {

  private static final Logger LOG = LoggerFactory.getLogger(RcaResource.class);

  private static final int DEFAULT_FORMATTER_DEPTH = 1;

  private static final long ANALYSIS_RANGE_MAX = TimeUnit.DAYS.toMillis(32);
  private static final long ANOMALY_RANGE_MAX = TimeUnit.DAYS.toMillis(32);
  private static final long BASELINE_RANGE_MAX = ANOMALY_RANGE_MAX;

  private final List<RootCauseEntityFormatter> formatters;
  private final Map<String, RCAFramework> frameworks;
  private final RootCauseTemplateResource rootCauseTemplateResource;
  private final RcaInvestigationResource rcaInvestigationResource;
  private final RootCauseMetricResource rootCauseMetricResource;
  private final DimensionAnalysisResource dimensionAnalysisResource;

  @Inject
  public RcaResource(
      final RootCauseAnalysisService rootCauseAnalysisService,
      final RootCauseTemplateResource rootCauseTemplateResource,
      final RcaInvestigationResource rcaInvestigationResource,
      final RootCauseMetricResource rootCauseMetricResource,
      final DimensionAnalysisResource dimensionAnalysisResource) {
    this.frameworks = rootCauseAnalysisService.getFrameworks();
    this.formatters = rootCauseAnalysisService.getFormatters();
    this.rootCauseTemplateResource = rootCauseTemplateResource;
    this.rcaInvestigationResource = rcaInvestigationResource;
    this.rootCauseMetricResource = rootCauseMetricResource;
    this.dimensionAnalysisResource = dimensionAnalysisResource;
  }

  @Path(value = "/dim-analysis")
  public DimensionAnalysisResource getDimensionAnalysisResource() {
    return dimensionAnalysisResource;
  }

  @Path(value = "/template")
  public RootCauseTemplateResource getRootCauseTemplateResource() {
    return rootCauseTemplateResource;
  }

  @Path(value = "/sessions")
  public RcaInvestigationResource getRootCauseSessionResource() {
    return rcaInvestigationResource;
  }

  @Path(value = "/metrics")
  public RootCauseMetricResource getRootCauseMetricResource() {
    return rootCauseMetricResource;
  }

  @GET
  @Path("/query")
  @ApiOperation(value = "Send query")
  public List<RootCauseEntity> query(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "framework name")
      @QueryParam("framework") String framework,
      @ApiParam(value = "start overall time window to consider for events")
      @QueryParam("analysisStart") Long analysisStart,
      @ApiParam(value = "end of overall time window to consider for events")
      @QueryParam("analysisEnd") Long analysisEnd,
      @ApiParam(value = "start time of the anomalous time period for the metric under analysis")
      @QueryParam("anomalyStart") Long anomalyStart,
      @ApiParam(value = "end time of the anomalous time period for the metric under analysis")
      @QueryParam("anomalyEnd") Long anomalyEnd,
      @ApiParam(value = "baseline start time, e.g. anomaly start time offset by 1 week")
      @QueryParam("baselineStart") Long baselineStart,
      @ApiParam(value = "baseline end time, e.g. typically anomaly start time offset by 1 week")
      @QueryParam("baselineEnd") Long baselineEnd,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @ApiParam(value = "URNs of metrics to analyze")
      @QueryParam("urns") List<String> urns) throws Exception {

    // configuration validation
    ensure(frameworks.containsKey(framework),
        String.format("Could not resolve framework '%s'. Allowed values: %s",
            framework,
            frameworks.keySet()));

    // input validation
    ensureExists(analysisStart,
        "Must provide analysis start timestamp (in milliseconds)");

    ensureExists(analysisEnd,
        "Must provide analysis end timestamp (in milliseconds)");

    if (anomalyStart == null) {
      anomalyStart = analysisStart;
    }

    if (anomalyEnd == null) {
      anomalyEnd = analysisEnd;
    }

    if (baselineStart == null) {
      baselineStart = anomalyStart - TimeUnit.DAYS.toMillis(7);
    }

    if (baselineEnd == null) {
      baselineEnd = anomalyEnd - TimeUnit.DAYS.toMillis(7);
    }

    if (formatterDepth == null) {
      formatterDepth = DEFAULT_FORMATTER_DEPTH;
    }

    ensure(analysisEnd - analysisStart <= ANALYSIS_RANGE_MAX,
        String.format("Analysis range cannot be longer than %d", ANALYSIS_RANGE_MAX));

    ensure(anomalyEnd - anomalyStart <= ANOMALY_RANGE_MAX,
        String.format("Anomaly range cannot be longer than %d", ANOMALY_RANGE_MAX));

    ensure(baselineEnd - baselineStart <= BASELINE_RANGE_MAX,
        String.format("Baseline range cannot be longer than %d", BASELINE_RANGE_MAX));

    // validate window size
    long anomalyWindow = anomalyEnd - anomalyStart;
    long baselineWindow = baselineEnd - baselineStart;
    ensure(anomalyWindow == baselineWindow,
        "Must provide equal-sized anomaly and baseline periods");

    // format inputs
    Set<Entity> inputs = new HashSet<>(Arrays.asList(
        TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANOMALY, anomalyStart, anomalyEnd),
        TimeRangeEntity.fromRange(0.8, TimeRangeEntity.TYPE_BASELINE, baselineStart, baselineEnd),
        TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANALYSIS, analysisStart, analysisEnd)
    ));

    for (String urn : urns) {
      inputs.add(EntityUtils.parseURN(urn, 1.0));
    }

    // run root-cause analysis
    RCAFrameworkExecutionResult result = frameworks.get(framework).run(inputs);

    // apply formatters
    return applyFormatters(result.getResultsSorted(), formatterDepth);
  }

  @GET
  @Path("/raw")
  @ApiOperation(value = "Raw")
  public List<RootCauseEntity> raw(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("framework") String framework,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @QueryParam("urns") List<String> urns) throws Exception {

    // configuration validation
    ensure(frameworks.containsKey(framework),
        String.format("Could not resolve framework '%s'. Allowed values: %s",
            framework,
            frameworks.keySet()));

    if (formatterDepth == null) {
      formatterDepth = DEFAULT_FORMATTER_DEPTH;
    }

    // format input
    Set<Entity> input = new HashSet<>();
    for (String urn : urns) {
      input.add(EntityUtils.parseURNRaw(urn, 1.0));
    }

    // run root-cause analysis
    RCAFrameworkExecutionResult result = this.frameworks.get(framework).run(input);

    // apply formatters
    return applyFormatters(result.getResultsSorted(), formatterDepth);
  }

  private List<RootCauseEntity> applyFormatters(Iterable<Entity> entities, int maxDepth) {
    List<RootCauseEntity> output = new ArrayList<>();
    for (Entity e : entities) {
      output.add(this.applyFormatters(e, maxDepth));
    }
    return output;
  }

  private RootCauseEntity applyFormatters(Entity e, int remainingDepth) {
    for (RootCauseEntityFormatter formatter : this.formatters) {
      if (formatter.applies(e)) {
        try {
          RootCauseEntity rce = formatter.format(e);

          if (remainingDepth > 1) {
            for (Entity re : e.getRelated()) {
              rce.addRelatedEntity(this.applyFormatters(re, remainingDepth - 1));
            }
          } else {
            // clear out any related entities added by the formatter by default
            rce.setRelatedEntities(Collections.emptyList());
          }

          return rce;
        } catch (Exception ex) {
          LOG.warn("Error applying formatter '{}'. Skipping.", formatter.getClass().getName(), ex);
        }
      }
    }
    throw new IllegalArgumentException(String.format("No formatter for Entity '%s'", e.getUrn()));
  }
}
