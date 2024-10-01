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
package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.NamespaceConfigurationApi;
import ai.startree.thirdeye.spi.api.TimeConfigurationApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TimeConfigurationDTO;
import org.testng.annotations.Test;

// This test validates the mapping between api <-> dto objects.
// If tests fail, you may need to delete thirdeye-core/target before rerunning.
public class ApiBeanMapperTest {

  @Test
  public void testToDataSourceApi() {
    final DataSourceDTO dto = new DataSourceDTO();
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final DataSourceApi gotApi = ApiBeanMapper.toApi(dto);
    assertThat(gotApi.getAuth()).isNotNull();
    assertThat(gotApi.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToDataSourceDto() {
    final DataSourceApi api = new DataSourceApi()
        .setAuth(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final DataSourceDTO gotDto = ApiBeanMapper.toDataSourceDto(api);
    assertThat(gotDto.getAuth()).isNotNull();
    assertThat(gotDto.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToDatasetApi() {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final DatasetApi gotApi = ApiBeanMapper.toApi(dto);
    assertThat(gotApi.getAuth()).isNotNull();
    assertThat(gotApi.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToDatasetDto() {
    final DatasetApi api = new DatasetApi()
        .setAuth(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final DatasetConfigDTO gotDto = ApiBeanMapper.toDatasetConfigDto(api);
    assertThat(gotDto.getAuth()).isNotNull();
    assertThat(gotDto.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToAlertTemplateApi() {
    final AlertTemplateDTO dto = new AlertTemplateDTO();
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final AlertTemplateApi gotApi = ApiBeanMapper.toAlertTemplateApi(dto);
    assertThat(gotApi.getAuth()).isNotNull();
    assertThat(gotApi.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToAlertTemplateDto() {
    final AlertTemplateApi api = new AlertTemplateApi()
        .setAuth(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final AlertTemplateDTO gotDto = ApiBeanMapper.toAlertTemplateDto(api);
    assertThat(gotDto.getAuth()).isNotNull();
    assertThat(gotDto.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToAlertApi() {
    final AlertDTO dto = new AlertDTO();
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final AlertApi gotApi = ApiBeanMapper.toApi(dto);
    assertThat(gotApi.getAuth()).isNotNull();
    assertThat(gotApi.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToAlertDto() {
    final AlertApi api = new AlertApi()
        .setAuth(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final AlertDTO gotDto = ApiBeanMapper.toAlertDto(api);
    assertThat(gotDto.getAuth()).isNotNull();
    assertThat(gotDto.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToEnumerationItemApi() {
    final EnumerationItemDTO dto = new EnumerationItemDTO();
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final EnumerationItemApi gotApi = ApiBeanMapper.toApi(dto);
    assertThat(gotApi.getAuth()).isNotNull();
    assertThat(gotApi.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToEnumerationItemDTO() {
    final EnumerationItemApi api = new EnumerationItemApi()
        .setAuth(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final EnumerationItemDTO gotDto = ApiBeanMapper.toEnumerationItemDTO(api);
    assertThat(gotDto.getAuth()).isNotNull();
    assertThat(gotDto.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToAuthorizationConfigurationApi() {
    final AuthorizationConfigurationDTO dto = new AuthorizationConfigurationDTO();
    dto.setNamespace("my-namespace");

    final AuthorizationConfigurationApi gotApi = ApiBeanMapper.toApi(dto);
    assertThat(gotApi.getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToAuthorizationConfigurationDTO() {
    final AuthorizationConfigurationApi api = new AuthorizationConfigurationApi()
        .setNamespace("my-namespace");

    final AuthorizationConfigurationDTO gotDto = ApiBeanMapper.toAuthorizationConfigurationDTO(api);
    assertThat(gotDto.getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToNamespaceConfigurationApi() {
    final NamespaceConfigurationDTO dto = new NamespaceConfigurationDTO();
    dto.setTimeConfiguration(new TimeConfigurationDTO()
        .setDateTimePattern(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN)
        .setTimezone(DEFAULT_CHRONOLOGY.getZone())
        .setMinimumOnboardingStartTime(946684800000L));
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final NamespaceConfigurationApi gotApi = ApiBeanMapper.toApi(dto);
    assertThat(gotApi).isNotNull();
    assertThat(gotApi.getTimeConfiguration()).isNotNull();
    assertThat(gotApi.getTimeConfiguration().getDateTimePattern())
        .isEqualTo(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN);
    assertThat(gotApi.getTimeConfiguration().getTimezone())
        .isEqualTo(DEFAULT_CHRONOLOGY.getZone());
    assertThat(gotApi.getTimeConfiguration().getMinimumOnboardingStartTime())
        .isEqualTo(946684800000L);
    assertThat(gotApi.getAuth().getNamespace()).isEqualTo("my-namespace");
  }

  @Test
  public void testToNamespaceConfigurationDTO() {
    final NamespaceConfigurationApi api = new NamespaceConfigurationApi();
    api.setTimeConfiguration(new TimeConfigurationApi()
        .setDateTimePattern(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN)
        .setTimezone(DEFAULT_CHRONOLOGY.getZone())
        .setMinimumOnboardingStartTime(946684800000L));
    api.setAuth(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final NamespaceConfigurationDTO gotDto = ApiBeanMapper.toNamespaceConfigurationDTO(api);
    assertThat(gotDto).isNotNull();
    assertThat(gotDto.getTimeConfiguration()).isNotNull();
    assertThat(gotDto.getTimeConfiguration().getDateTimePattern())
        .isEqualTo(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN);
    assertThat(gotDto.getTimeConfiguration().getTimezone())
        .isEqualTo(DEFAULT_CHRONOLOGY.getZone());
    assertThat(gotDto.getTimeConfiguration().getMinimumOnboardingStartTime())
        .isEqualTo(946684800000L);
    assertThat(gotDto.getAuth().getNamespace()).isEqualTo("my-namespace");
  }
}
