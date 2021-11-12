package org.apache.pinot.thirdeye.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisModuleConfig;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.RootcauseTemplateManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RootcauseTemplateDTO;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;

@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RootCauseTemplateResource {

  private final static String DIM_ANALYSIS_TEMPLATE_NAME_PREFIX = "dim_analysis::";
  private final static String DIM_ANALYSIS_MODULE_NAME_PREFIX = "dim_analysis::";
  private final static String POC_APPLICATION = "thirdeye-poc";
  private final static String RCA_MODULE_NAME = "name";
  private final static String RCA_MODULE_CONFIG = "configuration";

  private final MetricConfigManager metricConfigManager;
  private final RootcauseTemplateManager rootcauseTemplateManager;

  @Inject
  public RootCauseTemplateResource(final MetricConfigManager metricConfigManager,
      final RootcauseTemplateManager rootcauseTemplateManager) {
    this.metricConfigManager = metricConfigManager;
    this.rootcauseTemplateManager = rootcauseTemplateManager;
  }

  /**
   * Search RCA template based on metric ID, and it should be expanded to support different query
   * later.
   *
   * @param metricId metric ID
   * @return list of RCA templates
   */
  @GET
  @Path("/search")
  @ApiOperation(value = "Get root cause template")
  public List<RootcauseTemplateDTO> get(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("metricId") Long metricId) {
    if (metricId == null) {
      throw new IllegalArgumentException("Must provide valid metricId");
    }
    List<RootcauseTemplateDTO> templates = rootcauseTemplateManager.findByMetricId(metricId);
    if (templates == null) {
      templates = new ArrayList<>();
    }
    return templates;
  }

  /**
   * create a RCA template for single dimensional analysis module for RCA v2 POC.
   *
   * @param metricUrn metricURN for the template
   * @param dimensionStr comma separated string of list of included dimensions
   * @param excludeDimStr comma separated string of list of excluded dimensions
   * @param manualOrder flag to maintain order of included dimension direction
   * @param oneSideError flag to show dimension candidates with different direction
   * @param summarySize number of dimension candidates to be shown
   * @param dimensionDepth number of dimensions to be explored
   * @return id for the saved RCA template
   */
  @POST
  @Path("/saveDimensionAnalysis")
  public Long saveDimensionAnalysis(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("metricUrn") String metricUrn,
      @QueryParam("includedDimension") String dimensionStr,
      @QueryParam("excludedDimension") String excludeDimStr,
      @QueryParam("manualOrder") boolean manualOrder,
      @QueryParam("oneSideError") boolean oneSideError,
      @QueryParam("summarySize") int summarySize,
      @QueryParam("dimensionDepth") int dimensionDepth
  ) {
    ObjectMapper objMapper = new ObjectMapper();
    final String username = principal.getName();
    MetricEntity metricEntity = MetricEntity.fromURN(metricUrn);
    MetricConfigDTO metricConfigDTO = metricConfigManager.findById(metricEntity.getId());
    String templateName = DIM_ANALYSIS_TEMPLATE_NAME_PREFIX + metricConfigDTO.getAlias();
    RootcauseTemplateDTO rootcauseTemplateDTO = new RootcauseTemplateDTO();
    rootcauseTemplateDTO.setName(templateName);
    rootcauseTemplateDTO.setOwner(username);
    rootcauseTemplateDTO.setMetricId(metricEntity.getId());
    rootcauseTemplateDTO.setApplication(POC_APPLICATION);
    List<Map<String, Object>> modules = new ArrayList<>();
    Map<String, Object> dimAnalysisModule = new HashMap<>();
    dimAnalysisModule
        .put(RCA_MODULE_NAME, DIM_ANALYSIS_MODULE_NAME_PREFIX + metricConfigDTO.getAlias());
    DimensionAnalysisModuleConfig dimAnalysisModuleConfig = new DimensionAnalysisModuleConfig();
    if (!Strings.isNullOrEmpty(dimensionStr)) {
      dimAnalysisModuleConfig.setIncludedDimension(Arrays.asList(dimensionStr.split(",")));
    } else {
      dimAnalysisModuleConfig.setIncludedDimension(Collections.emptyList());
    }
    if (!Strings.isNullOrEmpty(excludeDimStr)) {
      dimAnalysisModuleConfig.setIncludedDimension(Arrays.asList(excludeDimStr.split(",")));
    } else {
      dimAnalysisModuleConfig.setExcludedDimension(Collections.emptyList());
    }
    dimAnalysisModuleConfig.setManualOrder(manualOrder);
    dimAnalysisModuleConfig.setOneSideError(oneSideError);
    dimAnalysisModuleConfig.setSummarySize(summarySize);
    dimAnalysisModuleConfig.setDimensionDepth(dimensionDepth);
    dimAnalysisModule
        .put(RCA_MODULE_CONFIG, objMapper.convertValue(dimAnalysisModuleConfig, Map.class));
    modules.add(dimAnalysisModule);
    rootcauseTemplateDTO.setModules(modules);
    return rootcauseTemplateManager.saveOrUpdate(rootcauseTemplateDTO);
  }
}
