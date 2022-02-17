/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaMetadataDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Objects;

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
    //startTime/endTime not important
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(
        alertDTO, 0L, 0L);
    final RcaMetadataDTO rcaMetadataDTO = Objects.requireNonNull(templateWithProperties.getRca(),
        "rca not found in alert config.");
    final String metric = Objects.requireNonNull(rcaMetadataDTO.getMetric(),
        "rca$metric not found in alert config.");
    final String dataset = Objects.requireNonNull(rcaMetadataDTO.getDataset(),
        "rca$dataset not found in alert config.");
    final MetricConfigDTO metricConfigDTO = ensureExists(
        metricDAO.findByMetricAndDataset(metric, dataset),
        String.format(
            "Could not find metric %s for dataset %s. Invalid RCA configuration for the alert %s?",
            metric,
            dataset,
            anomalyId));

    final DatasetConfigDTO datasetConfigDTO = ensureExists(datasetDAO.findByDataset(metricConfigDTO.getDataset()),
        String.format("Dataset name: %s", metricConfigDTO.getDataset()));

    return new RootCauseAnalysisInfo(anomalyDTO, metricConfigDTO, datasetConfigDTO);
  }
}
