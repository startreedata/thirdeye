/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator.getDateTimeZone;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RcaInfoFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(RcaInfoFetcher.class);

  private final MergedAnomalyResultManager mergedAnomalyDAO;
  private final AlertManager alertDAO;
  private final DatasetConfigManager datasetDAO;
  private final MetricConfigManager metricDAO;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public RcaInfoFetcher(
      final MergedAnomalyResultManager mergedAnomalyDAO,
      final AlertManager alertDAO,
      final DatasetConfigManager datasetDAO,
      final MetricConfigManager metricDAO,
      final AlertTemplateRenderer alertTemplateRenderer
  ) {
    this.mergedAnomalyDAO = mergedAnomalyDAO;
    this.alertDAO = alertDAO;
    this.datasetDAO = datasetDAO;
    this.metricDAO = metricDAO;
    this.alertTemplateRenderer = alertTemplateRenderer;
  }

  /**
   * Get the metricDTO to use for RCA from an anomalyId.
   *
   * Notes:
   * A small hack is used to get RCA working quickly: RCA configuration is passed in the alert
   * template.
   * This method gets the metric and dataset info from the alert template.
   * It could be more intelligent: metric and dataset could be inferred from the query.
   */
  public RootCauseAnalysisInfo getRootCauseAnalysisInfo(long anomalyId)
      throws IOException, ClassNotFoundException {
    final MergedAnomalyResultDTO anomalyDTO = ensureExists(
        mergedAnomalyDAO.findById(anomalyId), String.format("Anomaly ID: %d", anomalyId));
    final long detectionConfigId = anomalyDTO.getDetectionConfigId();
    final AlertDTO alertDTO = alertDAO.findById(detectionConfigId);

    // render properties - detectionInterval not important
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(
        alertDTO, new Interval(0L, 0L, DateTimeZone.UTC));
    // parse metadata
    AlertMetadataDTO alertMetadataDto = ensureExists(templateWithProperties.getMetadata(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata.");
    final MetricConfigDTO metadataMetricDTO = ensureExists(alertMetadataDto.getMetric(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$metric");
    final String metricName = ensureExists(metadataMetricDTO.getName(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$metric$name");
    final DatasetConfigDTO metadataDatasetDTO = ensureExists(alertMetadataDto.getDataset(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$dataset");
    final String datasetName = ensureExists(metadataDatasetDTO.getDataset(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$dataset$name");

    // take config from persistence - ensure dataset/metric DTO configs are correct for RCA
    MetricConfigDTO metricConfigDTO = metricDAO.findByMetricAndDataset(metricName, datasetName);
    if (metricConfigDTO == null) {
      LOG.warn("Could not find metric %s for dataset %s. Building a custom metric for RCA.");
      final String metricAggFunction = metadataMetricDTO.getDefaultAggFunction();
      ensure(StringUtils.isNotBlank(metricAggFunction), ERR_MISSING_CONFIGURATION_FIELD,
          String.format(
              "metadata$metric$aggregationFunction. It must be set when using a custom metric. Possible values: %s",
              List.of(MetricAggFunction.values())));
      metricConfigDTO = new MetricConfigDTO().setDataset(datasetName)
          .setName(metricName)
          .setDefaultAggFunction(metricAggFunction);
    }
    final DatasetConfigDTO datasetConfigDTO = ensureExists(datasetDAO.findByDataset(metricConfigDTO.getDataset()),
        String.format("Dataset name: %s", metricConfigDTO.getDataset()));
    addCustomFields(metricConfigDTO, metadataMetricDTO);
    addCustomFields(datasetConfigDTO, metadataDatasetDTO);

    final DateTimeZone timeZone = Optional.ofNullable(getDateTimeZone(templateWithProperties))
        .orElse(Constants.DEFAULT_TIMEZONE);

    return new RootCauseAnalysisInfo(anomalyDTO, metricConfigDTO, datasetConfigDTO, timeZone);
  }

  private void addCustomFields(final DatasetConfigDTO datasetConfigDTO,
      final DatasetConfigDTO metadataDatasetDTO) {
    // fields that can be configured at the alert level can be added here
    Optional.ofNullable(metadataDatasetDTO.getDimensions())
        .ifPresent(datasetConfigDTO::setDimensions);
    Optional.ofNullable(metadataDatasetDTO.getRcaExcludedDimensions())
        .ifPresent(datasetConfigDTO::setRcaExcludedDimensions);
  }

  private void addCustomFields(final MetricConfigDTO metricConfigDTO,
      final MetricConfigDTO metadataMetricDTO) {
    // fields that can be configured at the alert level can be added here
    Optional.ofNullable(metadataMetricDTO.getWhere()).ifPresent(metricConfigDTO::setWhere);
    if (StringUtils.isNotBlank(metadataMetricDTO.getDefaultAggFunction())) {
      metricConfigDTO.setDefaultAggFunction(metadataMetricDTO.getDefaultAggFunction());
    }
  }
}
