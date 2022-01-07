package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_CUBE_DEPTH_STRING;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_CUBE_SUMMARY_SIZE_STRING;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_HIERARCHIES;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_ONE_SIDE_ERROR;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.google.common.collect.ImmutableList;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.alert.AlertTemplateRenderer;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RcaMetadataDTO;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class DimensionAnalysisResource {

  private static final Logger LOG = LoggerFactory.getLogger(DimensionAnalysisResource.class);

  private final MergedAnomalyResultManager mergedAnomalyDAO;
  private final AlertManager alertDAO;
  private final DatasetConfigManager datasetDAO;
  private final MetricConfigManager metricDAO;
  private final AlertTemplateRenderer alertTemplateRenderer;
  private final DataCubeSummaryCalculator dataCubeSummaryCalculator;

  @Inject
  public DimensionAnalysisResource(
      final MergedAnomalyResultManager mergedAnomalyDAO,
      final AlertManager alertDAO,
      final DatasetConfigManager datasetDAO,
      final MetricConfigManager metricDAO,
      final AlertTemplateRenderer alertTemplateRenderer,
      final DataCubeSummaryCalculator dataCubeSummaryCalculator) {
    this.mergedAnomalyDAO = mergedAnomalyDAO;
    this.alertDAO = alertDAO;
    this.datasetDAO = datasetDAO;
    this.metricDAO = metricDAO;
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.dataCubeSummaryCalculator = dataCubeSummaryCalculator;
  }

  @GET
  @Path("anomaly/{id}")
  @ApiOperation("Retrieve the likely root causes behind an anomaly")
  public Response dataCubeSummary(
      //fixme cyril doc
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @PathParam("id") long id,
      @QueryParam("baselineStart") @DefaultValue("-1") long baselineStartInclusive,
      @QueryParam("baselineEnd") @DefaultValue("-1") long baselineEndExclusive,
      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")")
      @QueryParam("timezone") @DefaultValue(RootCauseMetricResource.TIMEZONE_DEFAULT) String timezone,
      @QueryParam("summarySize") @DefaultValue(DEFAULT_CUBE_SUMMARY_SIZE_STRING) @Min(value = 1) int summarySize,
      @QueryParam("depth") @DefaultValue(DEFAULT_CUBE_DEPTH_STRING) int depth,
      @QueryParam("oneSideError") @DefaultValue(DEFAULT_ONE_SIDE_ERROR) boolean doOneSideError,
      @QueryParam("dimensions") List<String> dimensions,
      @QueryParam("excludedDimensions") List<String> excludedDimensions,
      @QueryParam("hierarchies") @DefaultValue(DEFAULT_HIERARCHIES) String hierarchiesPayload
  ) throws Exception {
    DateTimeZone dateTimeZone = RootCauseMetricResource.parseTimeZone(timezone);

    // rewrite this as a metric/dataset info from anomalyId
    final MergedAnomalyResultDTO anomalyDTO = ensureExists(
        mergedAnomalyDAO.findById(id), String.format("Anomaly ID: %d", id));
    long detectionConfigId = anomalyDTO.getDetectionConfigId();
    AlertDTO alertDTO = alertDAO.findById(detectionConfigId);
    //startTime/endTime not important
    AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO, 0L, 0L);
    RcaMetadataDTO rcaMetadataDTO = Objects.requireNonNull(templateWithProperties.getRca(),
        "rca not found in alert config.");
    String metric = Objects.requireNonNull(rcaMetadataDTO.getMetric(),
        "rca$metric not found in alert config.");
    String dataset = Objects.requireNonNull(rcaMetadataDTO.getDataset(),
        "rca$dataset not found in alert config.");
    // fixme cyril managed null result below ?
    MetricConfigDTO metricConfigDTO = metricDAO.findByMetricAndDataset(metric, dataset);

    // todo cyril - refactored without changing timeZone usage - not sure if it is correct - timezone should be front only
    final Interval currentInterval = new Interval(anomalyDTO.getStartTime(), anomalyDTO.getEndTime(), dateTimeZone);
    final Interval baselineInterval = parseOrDefaultBaselineInterval(baselineStartInclusive,
        baselineEndExclusive,
        dateTimeZone,
        currentInterval);

    // fixme clean/move the dimensions cleaning
    final List<String> dimensionNames = dimensions.isEmpty() ?
        getDimensionsFromDataset(dataset) :
        DataCubeSummaryCalculator.cleanDimensionStrings(dimensions);
    dimensionNames.removeAll(DataCubeSummaryCalculator.cleanDimensionStrings(excludedDimensions));
    final Dimensions filteredDimensions = new Dimensions(dimensionNames);

    // fixme cyril implement hierarchies parsing
    final List<List<String>> hierarchies = ImmutableList.of(); //parseHierarchiesPayload(hierarchiesPayload);


    DimensionAnalysisResultApi resultApi = dataCubeSummaryCalculator.computeCube(
        metricConfigDTO.getName(),
        dataset,
        currentInterval,
        baselineInterval,
        summarySize,
        depth,
        doOneSideError,
        metricConfigDTO.getDerivedMetricExpression(),
        filteredDimensions,
        filters,
        hierarchies
    );

    // In the highlights api we retrieve only the top 3 results across 3 dimensions.
    return Response.ok(resultApi).build();
  }

  private Interval parseOrDefaultBaselineInterval(long baselineStartInclusive, long baselineEndExclusive,
      final DateTimeZone dateTimeZone, final Interval currentInterval) {
    if (baselineStartInclusive == -1 && baselineEndExclusive == -1) {
      // default baseline - 7 days offset
      baselineStartInclusive = currentInterval.getStartMillis() - TimeUnit.DAYS.toMillis(7);
      baselineEndExclusive = currentInterval.getEndMillis() - TimeUnit.DAYS.toMillis(7);
    } else if (baselineStartInclusive == -1 || baselineEndExclusive == -1) {
     throw new IllegalArgumentException("baselineStart and baselineEnd must be both custom or both default");
    }

    return new Interval(baselineStartInclusive,
        baselineEndExclusive,
        dateTimeZone);
  }

  private List<String> getDimensionsFromDataset(String datasetName) {
    DatasetConfigDTO datasetConfigDTO = datasetDAO.findByDataset(datasetName);
    if (datasetConfigDTO != null) {
      return datasetConfigDTO.getDimensions();
    }
    // fixme cyril cannot happen because called after findByMetricAndDataset
    throw new IllegalArgumentException(String.format("Unknown dataset %s. Cannot get dimensions.",
        datasetName));
  }
}
