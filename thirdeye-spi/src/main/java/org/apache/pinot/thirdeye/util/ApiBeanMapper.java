package org.apache.pinot.thirdeye.util;

import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.api.EmailSettingsApi;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.api.TimeColumnApi;
import org.apache.pinot.thirdeye.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DetectionAlertConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DetectionConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
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

  public static MetricApi toMetricApi(final MetricConfigDTO dto) {
    return new MetricApi()
        .setId(dto.getId())
        .setActive(dto.isActive())
        .setName(dto.getName())
        .setUpdated(dto.getUpdateTime())
        .setDataset(new DatasetApi()
            .setName(dto.getDataset())
        )
        ;
  }

  public static AlertApi toAlertApi(final DetectionConfigDTO dto) {
    return new AlertApi()
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setActive(dto.isActive())
        .setCron(dto.getCron())
        ;
  }

  public static SubscriptionGroupApi toSubscriptionGroupApi(final DetectionAlertConfigDTO dto) {
    final List<AlertApi> alertApis = optional(dto.getProperties())
        .map(o1 -> o1.get("detectionConfigIds"))
        .map(l -> ((List<Integer>) l).stream()
            .map(o -> new AlertApi().setId(Long.valueOf(o)))
            .collect(Collectors.toList()))
        .orElse(null);

    final EmailSettingsApi emailSettings = optional(dto.getAlertSchemes())
        .map(o -> o.get("emailScheme"))
        .map(o -> ((Map) o).get("recipients"))
        .map(m -> (Map) m)
        .map(m -> new EmailSettingsApi()
            .setTo(optional(m.get("to"))
                .map(l -> new ArrayList<>((List<String>) l))
                .orElse(null)
            )
            .setCc(optional(m.get("cc"))
                .map(l -> new ArrayList<>((List<String>) l))
                .orElse(null)
            )
            .setBcc(optional(m.get("bcc"))
                .map(l -> new ArrayList<>((List<String>) l))
                .orElse(null)
            ))
        .orElse(null);

    return new SubscriptionGroupApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setApplication(new ApplicationApi()
            .setName(dto.getApplication()))
        .setAlerts(alertApis)
        .setEmailSettings(emailSettings)
        ;
  }
}
