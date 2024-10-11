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
package ai.startree.thirdeye.datalayer;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;
import static java.util.stream.Collectors.toSet;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TimeConfigurationDTO;
import ai.startree.thirdeye.spi.metric.MetricType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DatalayerTestUtils {

  public static Set<Long> collectIds(final Collection<? extends AbstractDTO> anomalies) {
    return anomalies.stream().map(AbstractDTO::getId).collect(toSet());
  }


  public static MetricConfigDTO getTestMetricConfig(String collection, String metric, final String namespace, Long id) {
    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    if (id != null) {
      metricConfigDTO.setId(id);
    }
    metricConfigDTO.setDataset(collection);
    metricConfigDTO.setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace));
    metricConfigDTO.setDatatype(MetricType.LONG);
    metricConfigDTO.setName(metric);
    metricConfigDTO.setActive(Boolean.TRUE);
    metricConfigDTO.setAlias(SpiUtils.constructMetricAlias(collection, metric));
    return metricConfigDTO;
  }

  public static DatasetConfigDTO getTestDatasetConfig(String collection) {
    return new DatasetConfigDTO()
        .setDataset(collection)
        .setDimensions(Templatable.of(Lists.newArrayList("country", "browser", "environment")))
        .setTimeColumn("time")
        .setTimeDuration(1)
        .setTimeUnit(TimeUnit.HOURS)
        .setActive(true)
        .setDataSource("PinotThirdEyeDataSource")
        .setLastRefreshTime(System.currentTimeMillis());
  }

  public static NamespaceConfigurationDTO buildNamespaceConfiguration(String namespace) {
    final NamespaceConfigurationDTO dto = new NamespaceConfigurationDTO();
    dto.setTimeConfiguration(new TimeConfigurationDTO()
        .setDateTimePattern(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN)
        .setTimezone(DEFAULT_CHRONOLOGY.getZone())
        .setMinimumOnboardingStartTime(946684800000L));
    dto.setAuth(new AuthorizationConfigurationDTO()
        .setNamespace(namespace));
    return dto;
  }
}
