/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

@Singleton
public class RootCauseAnalysisInfoFetcher {

  private final MergedAnomalyResultManager mergedAnomalyDAO;
  private final AlertManager alertDAO;
  private final DatasetConfigManager datasetDAO;
  private final MetricConfigManager metricDAO;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public RootCauseAnalysisInfoFetcher(
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
        "metadata$dataset$dataset");

    // take config from persistence - makes it sure dataset/metric DTO configs are correct for RCA
    final MetricConfigDTO metricConfigDTO = ensureExists(
        metricDAO.findByMetricAndDataset(metricName, datasetName),
        String.format(
            "Could not find metric %s for dataset %s. Invalid RCA configuration for the alert %s?",
            metricName,
            datasetName,
            anomalyId));
    final DatasetConfigDTO datasetConfigDTO = ensureExists(datasetDAO.findByDataset(metricConfigDTO.getDataset()),
        String.format("Dataset name: %s", metricConfigDTO.getDataset()));

    addCustomFields(metricConfigDTO, metadataMetricDTO);
    addCustomFields(datasetConfigDTO, metadataDatasetDTO);

    return new RootCauseAnalysisInfo(anomalyDTO, metricConfigDTO, datasetConfigDTO);
  }

  private void addCustomFields(final DatasetConfigDTO datasetConfigDTO,
      final DatasetConfigDTO metadataDatasetDTO) {
    // fields that can be configured at the alert level can be added here
  }

  private void addCustomFields(final MetricConfigDTO metricConfigDTO,
      final MetricConfigDTO metadataMetricDTO) {
    // todo cyril add DefaultAggFunction to custom rcaInfoFetcher to allow custom aggregationfunction
    // fields that can be configured at the alert level can be added here
    Optional.ofNullable(metadataMetricDTO.getWhere()).ifPresent(metricConfigDTO::setWhere);
  }
}
