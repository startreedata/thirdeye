package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.mapper.ApiBeanMapper.toMetricApi;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertMetadataApi;
import ai.startree.thirdeye.spi.api.AlertNodeApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertNodeType;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.Date;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AnomalyMapper {

  AnomalyMapper INSTANCE = Mappers.getMapper(AnomalyMapper.class);

  default MergedAnomalyResultDTO toDto(AnomalyApi api) {
    if (api == null) {
      return null;
    }
    // incomplete because only used by RcaInvestigation for the moment
    return (MergedAnomalyResultDTO) new MergedAnomalyResultDTO().setId(api.getId());
  }

  default AnomalyApi toApi(MergedAnomalyResultDTO dto) {
    if (dto == null) {
      return null;
    }
    final MetricApi metricApi = optional(dto.getMetric())
        .map(metric -> new MetricApi().setName(metric))
        .orElse(null);

    final DatasetApi datasetApi = optional(dto.getCollection())
        .map(datasetName -> new DatasetApi().setName(datasetName))
        .orElse(null);

    final AnomalyApi anomalyApi = new AnomalyApi()
        .setId(dto.getId())
        .setStartTime(new Date(dto.getStartTime()))
        .setEndTime(new Date(dto.getEndTime()))
        .setCreated(new Date(dto.getCreatedTime()))
        .setAvgCurrentVal(dto.getAvgCurrentVal())
        .setAvgBaselineVal(dto.getAvgBaselineVal())
        .setScore(dto.getScore())
        .setWeight(dto.getWeight())
        .setImpactToGlobal(dto.getImpactToGlobal())
        .setSourceType(dto.getAnomalyResultSource())
        .setNotified(dto.isNotified())
        .setMessage(dto.getMessage())
        .setMetric(metricApi)
        .setMetadata(new AlertMetadataApi()
            .setMetric(metricApi)
            .setDataset(datasetApi)
        )
        ;
    if (dto.getMetricUrn() != null) {
      anomalyApi
          .setMetric(toMetricApi(dto.getMetricUrn())
              .setName(dto.getMetric())
              .setDataset(datasetApi)
          );
    }
    anomalyApi.setAlert(new AlertApi()
            .setId(dto.getDetectionConfigId())
        )
        .setAlertNode(optional(dto.getProperties())
            .map(p -> p.get("detectorComponentName"))
            .map(AnomalyMapper::toDetectionAlertNodeApi)
            .orElse(null))
        .setFeedback(optional(dto.getFeedback())
            .map(ApiBeanMapper::toApi)
            .orElse(null));
    return anomalyApi;
  }

  private static AlertNodeApi toDetectionAlertNodeApi(final String detectorComponentName) {
    final String[] splitted = detectorComponentName.split(":");
    checkState(splitted.length == 2);

    return new AlertNodeApi()
        .setName(splitted[0])
        .setType(AlertNodeType.DETECTION)
        .setSubType(splitted[1]);
  }
}
