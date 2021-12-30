package org.apache.pinot.thirdeye.mapper;

import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.UserApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertApiBeanMapper {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertApiBeanMapper.class);

  @Inject
  public AlertApiBeanMapper() {
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
        .map(ApiBeanMapper::toAlertTemplateDto)
        .ifPresent(dto::setTemplate);

    optional(api.getTemplateProperties())
        .ifPresent(dto::setTemplateProperties);

    // May not get updated while edits
    optional(api.getOwner())
        .map(UserApi::getPrincipal)
        .ifPresent(dto::setCreatedBy);

    if (api.getNodes() != null) {
      LOG.error("nodes field is not null, but legacy detection config is not supported anymore. Parsing skipped");
    }

    return dto;
  }
}
