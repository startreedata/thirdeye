package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.UserApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertApiBeanMapper {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertApiBeanMapper.class);

  private final DataProvider dataProvider;

  @Inject
  public AlertApiBeanMapper(
      final DataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  public AlertDTO toAlertDTO(final AlertApi api) {
    final AlertDTO dto = new AlertDTO();

    dto.setName(api.getName());
    dto.setDescription(api.getDescription());
    dto.setActive(optional(api.getActive()).orElse(true));
    dto.setCron(api.getCron());
    dto.setLastTimestamp(optional(api.getLastTimestamp())
        .map(d -> d.toInstant().toEpochMilli())
        .orElse(0L));
    dto.setUpdateTime(new Timestamp(System.currentTimeMillis()));

    optional(api.getNodes())
        .map(ApiBeanMapper::toAlertNodeMap)
        .ifPresent(dto::setNodes);

    optional(api.getTemplate())
        .map(ApiBeanMapper::toAlertTemplateBean)
        .ifPresent(dto::setTemplate);

    // May not get updated while edits
    optional(api.getOwner())
        .map(UserApi::getPrincipal)
        .ifPresent(dto::setCreatedBy);

    if (api.getNodes() != null) {
      final AlertExecutionPlanBuilder builder = new AlertExecutionPlanBuilder(dataProvider)
          .process(api);
      dto.setProperties(builder.getProperties());
      dto.setComponentSpecs(builder.getComponentSpecs());
    }

    return dto;
  }
}
