/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaMetadataDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
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
  private final AlertTemplateManager alertTemplateDAO;

  @Inject
  public RootCauseAnalysisInfoFetcher(
      final MergedAnomalyResultManager mergedAnomalyDAO,
      final AlertManager alertDAO,
      final DatasetConfigManager datasetDAO,
      final MetricConfigManager metricDAO,
      final AlertTemplateRenderer alertTemplateRenderer,
      final AlertTemplateManager alertTemplateDAO
  ) {
    this.mergedAnomalyDAO = mergedAnomalyDAO;
    this.alertDAO = alertDAO;
    this.datasetDAO = datasetDAO;
    this.metricDAO = metricDAO;
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.alertTemplateDAO = alertTemplateDAO;
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

    // todo cyril remove migration logic around May 2022
    if (alertDTO.getTemplate().getId() != null || alertDTO.getTemplate().getName() != null) {
      tryMigrateTemplateFromRcaToMetadata(alertDTO.getTemplate());
    } else {
      tryMigrateAlertFromRcaToMetadata(alertDTO);
    }

    // render properties - detectionInterval not important
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(
        alertDTO, new Interval(0L, 0L, DateTimeZone.UTC));
    // parse metadata
    AlertMetadataDTO alertMetadataDto = ensureExists(templateWithProperties.getMetadata(), ERR_MISSING_CONFIGURATION_FIELD,"metadata.");
    final MetricConfigDTO metadataMetricDTO = ensureExists(alertMetadataDto.getMetric(), ERR_MISSING_CONFIGURATION_FIELD, "metadata$metric");
    final String metricName = ensureExists(metadataMetricDTO.getName(), ERR_MISSING_CONFIGURATION_FIELD,"metadata$metric$name");
    final DatasetConfigDTO metadataDatasetDTO = ensureExists(alertMetadataDto.getDataset(), ERR_MISSING_CONFIGURATION_FIELD, "metadata$dataset");
    final String datasetName = ensureExists(metadataDatasetDTO.getDataset(), ERR_MISSING_CONFIGURATION_FIELD, "metadata$dataset$dataset");

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

  private void addCustomFields(final DatasetConfigDTO datasetConfigDTO, final DatasetConfigDTO metadataDatasetDTO) {
    // fields that can be configured at the alert level can be added here
  }

  private void addCustomFields(final MetricConfigDTO metricConfigDTO, final MetricConfigDTO metadataMetricDTO) {
    // todo cyril add DefaultAggFunction to custom rcaInfoFetcher to allow custom aggregationfunction
    // fields that can be configured at the alert level can be added here
    Optional.ofNullable(metadataMetricDTO.getWhere()).ifPresent(metricConfigDTO::setWhere);
  }

  private void tryMigrateTemplateFromRcaToMetadata(final AlertTemplateDTO alertTemplateInsideAlertDto) {
    // get template
    final AlertTemplateDTO templateDTO;
    final Long id = alertTemplateInsideAlertDto.getId();
    final String name = alertTemplateInsideAlertDto.getName();
    if (id != null) {
      templateDTO = alertTemplateDAO.findById(id);
    } else if (name != null) {
      final List<AlertTemplateDTO> byName = alertTemplateDAO.findByName(name);
      ensure(byName.size() == 1, ERR_OBJECT_DOES_NOT_EXIST, "template not found: " + name);
      templateDTO = byName.get(0);
    }
    else {
      // cannot happen in context
      throw new RuntimeException();
    }
    // migrate template
    if (templateDTO.getRca() != null) {
      replaceRcaByMetadata(templateDTO);
      int migrationSuccess = alertTemplateDAO.update(templateDTO);
      if (migrationSuccess != 1) {
        throw new RuntimeException("Error when migrating rca info to AlertMetadata for template");
      }
    }
  }

  private void tryMigrateAlertFromRcaToMetadata(final AlertDTO alertDTO) {
    // get template
    final AlertTemplateDTO templateDTO = alertDTO.getTemplate();
    // migrate alert
    if (templateDTO.getRca() != null) {
      replaceRcaByMetadata(templateDTO);
      int migrationSuccess = alertDAO.update(alertDTO);
      if (migrationSuccess != 1) {
        throw new RuntimeException("Error when migrating rca info to AlertMetadata for alert");
      }
    }
  }

  private void replaceRcaByMetadata(final AlertTemplateDTO templateDTO) {
    RcaMetadataDTO rcaMetadataDTO = templateDTO.getRca();
    DatasetConfigDTO metadataDatasetDTO = new DatasetConfigDTO();
    metadataDatasetDTO.setDataset(rcaMetadataDTO.getDataset());
    AlertMetadataDTO alertMetadataDTO = Optional.ofNullable(templateDTO.getMetadata()).orElse(new AlertMetadataDTO());
    templateDTO.setMetadata(alertMetadataDTO
        .setDataset(metadataDatasetDTO)
        .setDatasource(new DataSourceDTO().setName(rcaMetadataDTO.getDatasource()))
        .setMetric(new MetricConfigDTO().setName(rcaMetadataDTO.getMetric()))
    );
    templateDTO.setRca(null);
  }
}
