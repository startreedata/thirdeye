package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_EXCLUDED_DIMENSIONS;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_HIERARCHIES;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_TIMEZONE_ID;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.parseListParams;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import joptsimple.internal.Strings;
import org.apache.pinot.thirdeye.api.RootCauseEntity;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator;
import org.apache.pinot.thirdeye.rca.RootCauseEntityFormatter;
import org.apache.pinot.thirdeye.rootcause.Entity;
import org.apache.pinot.thirdeye.rootcause.RCAFramework;
import org.apache.pinot.thirdeye.rootcause.RCAFrameworkExecutionResult;
import org.apache.pinot.thirdeye.rootcause.impl.TimeRangeEntity;
import org.apache.pinot.thirdeye.rootcause.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(MediaType.APPLICATION_JSON)
public class RootCauseResource {
  private static final Logger LOG = LoggerFactory.getLogger(RootCauseResource.class);

  private static final int DEFAULT_FORMATTER_DEPTH = 1;

  private static final long ANALYSIS_RANGE_MAX = TimeUnit.DAYS.toMillis(32);
  private static final long ANOMALY_RANGE_MAX = TimeUnit.DAYS.toMillis(32);
  private static final long BASELINE_RANGE_MAX = ANOMALY_RANGE_MAX;
  private static final int DEFAULT_HIGHLIGHT_CUBE_SUMMARY_SIZE = 4;
  private static final int DEFAULT_HIGHLIGHT_CUBE_DEPTH = 3;

  private final List<RootCauseEntityFormatter> formatters;
  private final Map<String, RCAFramework> frameworks;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final DataCubeSummaryCalculator dataCubeSummaryCalculator;

  public RootCauseResource(Map<String, RCAFramework> frameworks,
      List<RootCauseEntityFormatter> formatters,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final DataCubeSummaryCalculator dataCubeSummaryCalculator) {
    this.frameworks = frameworks;
    this.formatters = formatters;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.dataCubeSummaryCalculator = dataCubeSummaryCalculator;
  }

  @GET
  @Path("/highlights")
  @ApiOperation("Retrieve the likely root causes behind an anomaly")
  public Map<String, Object> highlights(
      @ApiParam(value = "internal id of the anomaly")
      @QueryParam("anomalyId") long anomalyId,
      @Auth ThirdEyePrincipal principal) throws Exception {
    Map<String, Object> responseMessage = new HashMap<>();
    MergedAnomalyResultDTO anomalyDTO = mergedAnomalyResultManager.findById(anomalyId);
    Preconditions.checkNotNull(anomalyDTO, "Anomaly doesn't exist in ThirdEye's repository");

    // In the highlights api we retrieve only the top 3 results across 3 dimensions.
    // TODO: polish the results to make it more meaningful
    Map<String, Object> cubeHighlights = dataCubeSummaryCalculator.getDataCubeSummary(
        anomalyDTO.getMetricUrn(),
        anomalyDTO.getCollection(),
        anomalyDTO.getMetric(),
        anomalyDTO.getStartTime(),
        anomalyDTO.getEndTime(),
        anomalyDTO.getStartTime() - TimeUnit.DAYS.toMillis(7),
        anomalyDTO.getEndTime() - TimeUnit.DAYS.toMillis(7),
        Strings.EMPTY,
        Strings.EMPTY,
        DEFAULT_HIGHLIGHT_CUBE_SUMMARY_SIZE,
        DEFAULT_HIGHLIGHT_CUBE_DEPTH,
        DEFAULT_HIERARCHIES,
        false,
        DEFAULT_EXCLUDED_DIMENSIONS,
        DEFAULT_TIMEZONE_ID);
    responseMessage.put("cubeResults", cubeHighlights);

    return responseMessage;
  }

  @GET
  @Path("/query")
  @ApiOperation(value = "Send query")
  public List<RootCauseEntity> query(
      @ApiParam(value = "framework name")
      @QueryParam("framework") String framework,
      @ApiParam(value = "start overall time window to consider for events")
      @QueryParam("analysisStart") Long analysisStart,
      @ApiParam(value = "end of overall time window to consider for events")
      @QueryParam("analysisEnd") Long analysisEnd,
      @ApiParam(value = "start time of the anomalous time period for the metric under analysis", defaultValue = "analysisStart")
      @QueryParam("anomalyStart") Long anomalyStart,
      @ApiParam(value = "end time of the anomalous time period for the metric under analysis", defaultValue = "analysisEnd")
      @QueryParam("anomalyEnd") Long anomalyEnd,
      @ApiParam(value = "baseline start time, e.g. anomaly start time offset by 1 week", defaultValue = "anomalyStart - 7 days")
      @QueryParam("baselineStart") Long baselineStart,
      @ApiParam(value = "baseline end time, e.g. typically anomaly start time offset by 1 week", defaultValue = "anomalyEnd - 7 days")
      @QueryParam("baselineEnd") Long baselineEnd,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @ApiParam(value = "URNs of metrics to analyze")
      @QueryParam("urns") List<String> urns) throws Exception {

    // configuration validation
    if(!this.frameworks.containsKey(framework))
      throw new IllegalArgumentException(String.format("Could not resolve framework '%s'", framework));

    // input validation
    if(analysisStart == null)
      throw new IllegalArgumentException("Must provide analysis start timestamp (in milliseconds)");

    if(analysisEnd == null)
      throw new IllegalArgumentException("Must provide analysis end timestamp (in milliseconds)");

    if(anomalyStart == null)
      anomalyStart = analysisStart;

    if(anomalyEnd == null)
      anomalyEnd = analysisEnd;

    if(baselineStart == null)
      baselineStart = anomalyStart - TimeUnit.DAYS.toMillis(7);

    if(baselineEnd == null)
      baselineEnd = anomalyEnd - TimeUnit.DAYS.toMillis(7);

    if(formatterDepth == null)
      formatterDepth = DEFAULT_FORMATTER_DEPTH;

    if(analysisEnd - analysisStart > ANALYSIS_RANGE_MAX)
      throw new IllegalArgumentException(String.format("Analysis range cannot be longer than %d", ANALYSIS_RANGE_MAX));

    if(anomalyEnd - anomalyStart > ANOMALY_RANGE_MAX)
      throw new IllegalArgumentException(String.format("Anomaly range cannot be longer than %d", ANOMALY_RANGE_MAX));

    if(baselineEnd - baselineStart > BASELINE_RANGE_MAX)
      throw new IllegalArgumentException(String.format("Baseline range cannot be longer than %d", BASELINE_RANGE_MAX));

    urns = parseListParams(urns);

    // validate window size
    long anomalyWindow = anomalyEnd - anomalyStart;
    long baselineWindow = baselineEnd - baselineStart;
    if(anomalyWindow != baselineWindow)
      throw new IllegalArgumentException("Must provide equal-sized anomaly and baseline periods");

    // format input
    Set<Entity> input = new HashSet<>();
    input.add(TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANOMALY, anomalyStart, anomalyEnd));
    input.add(TimeRangeEntity.fromRange(0.8, TimeRangeEntity.TYPE_BASELINE, baselineStart, baselineEnd));
    input.add(TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANALYSIS, analysisStart, analysisEnd));
    for(String urn : urns) {
      input.add(EntityUtils.parseURN(urn, 1.0));
    }

    // run root-cause analysis
    RCAFrameworkExecutionResult result = this.frameworks.get(framework).run(input);

    // apply formatters
    return applyFormatters(result.getResultsSorted(), formatterDepth);
  }

  @GET
  @Path("/raw")
  @ApiOperation(value = "Raw")
  public List<RootCauseEntity> raw(
      @QueryParam("framework") String framework,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @QueryParam("urns") List<String> urns) throws Exception {

    // configuration validation
    if(!this.frameworks.containsKey(framework))
      throw new IllegalArgumentException(String.format("Could not resolve framework '%s'", framework));

    if(formatterDepth == null)
      formatterDepth = DEFAULT_FORMATTER_DEPTH;

    // parse urns arg
    urns = parseListParams(urns);

    // format input
    Set<Entity> input = new HashSet<>();
    for(String urn : urns) {
      input.add(EntityUtils.parseURNRaw(urn, 1.0));
    }

    // run root-cause analysis
    RCAFrameworkExecutionResult result = this.frameworks.get(framework).run(input);

    // apply formatters
    return applyFormatters(result.getResultsSorted(), formatterDepth);
  }

  private List<RootCauseEntity> applyFormatters(Iterable<Entity> entities, int maxDepth) {
    List<RootCauseEntity> output = new ArrayList<>();
    for(Entity e : entities) {
      output.add(this.applyFormatters(e, maxDepth));
    }
    return output;
  }

  private RootCauseEntity applyFormatters(Entity e, int remainingDepth) {
    for(RootCauseEntityFormatter formatter : this.formatters) {
      if(formatter.applies(e)) {
        try {
          RootCauseEntity rce = formatter.format(e);

          if(remainingDepth > 1) {
            for (Entity re : e.getRelated()) {
              rce.addRelatedEntity(this.applyFormatters(re, remainingDepth - 1));
            }
          } else {
            // clear out any related entities added by the formatter by default
            rce.setRelatedEntities(Collections.<RootCauseEntity>emptyList());
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
