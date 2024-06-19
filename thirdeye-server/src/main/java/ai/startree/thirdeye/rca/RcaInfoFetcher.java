/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATASET_NOT_FOUND_IN_NAMESPACE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.spi.metric.MetricAggFunction.COUNT;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getDateTimeZone;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventContextDto;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Chronology;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer of this class should ensure authz is performed.
 */
@Singleton
public class RcaInfoFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(RcaInfoFetcher.class);
  private static final EventContextDto EMPTY_CONTEXT_DTO = new EventContextDto();
  private final AlertManager alertDAO;
  private final DatasetConfigManager datasetDAO;
  private final MetricConfigManager metricDAO;
  private final AlertTemplateRenderer alertTemplateRenderer;
  private final EnumerationItemManager enumerationItemManager;
  private final DataSourceManager dataSourceManager;

  @Inject
  public RcaInfoFetcher(
      final AlertManager alertDAO,
      final DatasetConfigManager datasetDAO,
      final MetricConfigManager metricDAO,
      final AlertTemplateRenderer alertTemplateRenderer,
      final EnumerationItemManager enumerationItemManager,
      final DataSourceManager dataSourceManager) {
    this.alertDAO = alertDAO;
    this.datasetDAO = datasetDAO;
    this.metricDAO = metricDAO;
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.enumerationItemManager = enumerationItemManager;
    this.dataSourceManager = dataSourceManager;
  }

  private static <E> boolean templatableListIsNotEmpty(
      final Templatable<List<E>> metadataDatasetDTO) {
    return optional(metadataDatasetDTO).map(
        Templatable::getValue).map(l -> !l.isEmpty()).orElse(false);
  }

  /**
   * Get the metricDTO to use for RCA from an anomalyId.
   *
   * Notes:
   * A small hack is used to get RCA working quickly: RCA configuration is passed in the alert
   * template.
   * This method gets the metric and dataset info from the alert template.
   * It could be more intelligent: metric and dataset could be inferred from the query.
   *
   * Consumer of this method should ensure authz on the anomaly entity.
   */
  public RcaInfo getRcaInfo(final AnomalyDTO anomalyDTO)
      throws IOException, ClassNotFoundException {
    final long detectionConfigId = anomalyDTO.getDetectionConfigId();
    final AlertDTO alertDTO = alertDAO.findById(detectionConfigId);
    final EnumerationItemDTO enumerationItemDTO = optional(anomalyDTO.getEnumerationItem())
        .map(AbstractDTO::getId)
        .map(enumerationItemManager::findById)
        .orElse(null);

    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO,
        enumerationItemDTO);

    final AlertMetadataDTO alertMetadataDto = ensureExists(templateWithProperties.getMetadata(),
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
    // we get the namespace from the alert and inject it in all persistence access - authz is not performed - the consumer of this method should ensure authz on the anomaly
    final String alertNamespace = alertDTO.namespace();
    MetricConfigDTO metricConfigDTO = metricDAO.findBy(metricName, datasetName, alertNamespace);
    if (metricConfigDTO == null) {
      LOG.warn("Could not find metric {} for dataset {}. Building a custom metric for RCA.",
          metricName, datasetName);
      String metricAggFunction = metadataMetricDTO.getDefaultAggFunction();
      if (StringUtils.isBlank(metricAggFunction)) {
        LOG.warn(
            "Custom aggregation function not provided in alert configuration for metric {} for dataset {}. Defaulting to COUNT",
            metricName, datasetName);
        metricAggFunction = COUNT.toString();
      }
      metricConfigDTO = new MetricConfigDTO().setDataset(datasetName)
          .setName(metricName)
          .setDefaultAggFunction(metricAggFunction);
    }

    final DatasetConfigDTO datasetConfigDto;
    if (metricConfigDTO.getDatasetConfig() != null) {
      datasetConfigDto = datasetDAO.findById(metricConfigDTO.getDatasetConfig().getId());
    } else {
      LOG.warn(
          "Running on a legacy metrics that does not contain the id of its parent dataset. Finding dataset by name and namespace.");
      datasetConfigDto = datasetDAO.findByNameAndNamespaceOrUnsetNamespace(
          metricConfigDTO.getDataset(),
          alertNamespace);
    }
    ensureExists(datasetConfigDto, ERR_DATASET_NOT_FOUND_IN_NAMESPACE, metricConfigDTO.getDataset(),
        alertNamespace);
    addCustomFields(metricConfigDTO, metadataMetricDTO);
    addCustomFields(datasetConfigDto, metadataDatasetDTO);

    final DataSourceDTO dataSourceDto = dataSourceManager.findUniqueByNameAndNamespace(
        datasetConfigDto.getDataSource(), datasetConfigDto.namespace());
    ensureExists(dataSourceDto, "Datasource name: %s. Namespace: %s".formatted(
        datasetConfigDto.getDataSource(),
        datasetConfigDto.namespace()));

    final Period granularity = isoPeriod(alertMetadataDto.getGranularity());
    final Chronology chronology = getDateTimeZone(alertMetadataDto);
    EventContextDto eventContext = alertMetadataDto.getEventContext();
    if (eventContext == null || eventContext.equals(EMPTY_CONTEXT_DTO)) {
      // fixme suvodeep cyril findFromAlert is a quick hack for a client - to remove once templates are updated
      eventContext = optional(findFromAlert(alertDTO, anomalyDTO.getEnumerationItem())).orElse(
          EMPTY_CONTEXT_DTO);
    }

    return new RcaInfo(anomalyDTO, alertDTO, metricConfigDTO, datasetConfigDto, dataSourceDto,
        chronology,
        granularity,
        eventContext);
  }

  @SuppressWarnings("unchecked")
  private EventContextDto findFromAlert(final AlertDTO alertDTO,
      final EnumerationItemDTO enumerationItem) {
    final Map<String, Object> properties = optional(enumerationItem)
        .map(EnumerationItemDTO::getParams)
        .orElse(alertDTO.getTemplateProperties());
    try {
      final List<String> eventTypes = (List<String>) properties.get("eventTypes");
      final String eventSqlFilter = (String) properties.get("eventSqlFilter");
      if (eventTypes != null || eventSqlFilter != null) {
        return new EventContextDto()
            .setTypes(Templatable.of(eventTypes))
            .setSqlFilter(eventSqlFilter);
      }
    } catch (Exception e) {
      LOG.error("error applying eventContext on anomaly! alert id: " + alertDTO.getId(), e);
    }
    return null;
  }

  @VisibleForTesting
  protected static void addCustomFields(final DatasetConfigDTO dataset,
      final DatasetConfigDTO metadataDataset) {
    // fields that can be configured at the alert level are parsed in this method
    final boolean includedListIsNotEmpty = templatableListIsNotEmpty(
        metadataDataset.getDimensions());
    final boolean excludedListIsNotEmpty = templatableListIsNotEmpty(
        metadataDataset.getRcaExcludedDimensions());
    checkArgument(!(includedListIsNotEmpty && excludedListIsNotEmpty),
        "Both dimensions and rcaExcludedDimensions are not empty. Cannot use an inclusion and an exclusion list at the same time.");
    if (excludedListIsNotEmpty) {
      dataset.setRcaExcludedDimensions(metadataDataset.getRcaExcludedDimensions());
    }
    if (includedListIsNotEmpty) {
      dataset.setDimensions(metadataDataset.getDimensions());
    }
  }

  private static void addCustomFields(final MetricConfigDTO metricConfigDTO,
      final MetricConfigDTO metadataMetricDTO) {
    // fields that can be configured at the alert level can be added here
    optional(metadataMetricDTO.getWhere()).ifPresent(metricConfigDTO::setWhere);
    optional(metadataMetricDTO.getDefaultAggFunction()).filter(StringUtils::isNotBlank)
        .ifPresent(metricConfigDTO::setDefaultAggFunction);
  }
}
