package org.apache.pinot.thirdeye.util;

import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.api.TimeColumnApi;
import org.apache.pinot.thirdeye.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.pojo.ApplicationBean;

public abstract class ApiBeanMapper {

  public static ApplicationApi toApplicationApi(final ApplicationBean o) {
    return new ApplicationApi()
        .setId(o.getId())
        .setName(o.getApplication())
        ;
  }

  public static ApplicationDTO toApplicationDto(final ApplicationApi applicationApi) {
    final ApplicationDTO applicationDTO = new ApplicationDTO();
    applicationDTO.setApplication(applicationApi.getName());
    applicationDTO.setRecipients("");
    return applicationDTO;
  }

  public static DatasetApi toDatasetApi(final DatasetConfigDTO dto) {
    return new DatasetApi()
        .setId(dto.getId())
        .setActive(dto.isActive())
        .setAdditive(dto.isAdditive())
        .setDimensions(dto.getDimensions())
        .setName(dto.getDataset())
        .setTimeColumn(new TimeColumnApi()
            .setName(dto.getTimeColumn())
            .setInterval(dto.bucketTimeGranularity().toDuration())
            .setFormat(dto.getTimeFormat())
            .setTimezone(dto.getTimezone())
        )
        .setExpectedDelay(dto.getExpectedDelay().toDuration())
        ;
  }
}
