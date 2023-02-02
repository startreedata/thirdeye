/*
 * Copyright 2023 StarTree Inc
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

import static org.testng.Assert.*;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import org.testng.annotations.Test;

// This test validates the mapping between api <-> dto objects.
// If tests fail, you may need to delete thirdeye-core/target before rerunning.
public class ApiBeanMapperTest {

  @Test
  public void testToDataSourceApi() {
    final DataSourceDTO dto = new DataSourceDTO();
    dto.setAuthorization(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final DataSourceApi gotApi = ApiBeanMapper.toApi(dto);
    assertNotNull(gotApi.getAuthorization());
    assertEquals(gotApi.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToDataSourceDto() {
    final DataSourceApi api = new DataSourceApi()
        .setAuthorization(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final DataSourceDTO gotDto = ApiBeanMapper.toDataSourceDto(api);
    assertNotNull(gotDto.getAuthorization());
    assertEquals(gotDto.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToDatasetApi() {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    dto.setAuthorization(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final DatasetApi gotApi = ApiBeanMapper.toApi(dto);
    assertNotNull(gotApi.getAuthorization());
    assertEquals(gotApi.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToDatasetDto() {
    final DatasetApi api = new DatasetApi()
        .setAuthorization(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final DatasetConfigDTO gotDto = ApiBeanMapper.toDatasetConfigDto(api);
    assertNotNull(gotDto.getAuthorization());
    assertEquals(gotDto.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToAlertTemplateApi() {
    final AlertTemplateDTO dto = new AlertTemplateDTO();
    dto.setAuthorization(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final AlertTemplateApi gotApi = ApiBeanMapper.toAlertTemplateApi(dto);
    assertNotNull(gotApi.getAuthorization());
    assertEquals(gotApi.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToAlertTemplateDto() {
    final AlertTemplateApi api = new AlertTemplateApi()
        .setAuthorization(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final AlertTemplateDTO gotDto = ApiBeanMapper.toAlertTemplateDto(api);
    assertNotNull(gotDto.getAuthorization());
    assertEquals(gotDto.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToAlertApi() {
    final AlertDTO dto = new AlertDTO();
    dto.setAuthorization(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final AlertApi gotApi = ApiBeanMapper.toApi(dto);
    assertNotNull(gotApi.getAuthorization());
    assertEquals(gotApi.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToAlertDto() {
    final AlertApi api = new AlertApi()
        .setAuthorization(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final AlertDTO gotDto = ApiBeanMapper.toAlertDto(api);
    assertNotNull(gotDto.getAuthorization());
    assertEquals(gotDto.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToEnumerationItemApi() {
    final EnumerationItemDTO dto = new EnumerationItemDTO();
    dto.setAuthorization(new AuthorizationConfigurationDTO().setNamespace("my-namespace"));

    final EnumerationItemApi gotApi = ApiBeanMapper.toApi(dto);
    assertNotNull(gotApi.getAuthorization());
    assertEquals(gotApi.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToEnumerationItemDTO() {
    final EnumerationItemApi api = new EnumerationItemApi()
        .setAuthorization(new AuthorizationConfigurationApi().setNamespace("my-namespace"));

    final EnumerationItemDTO gotDto = ApiBeanMapper.toEnumerationItemDTO(api);
    assertNotNull(gotDto.getAuthorization());
    assertEquals(gotDto.getAuthorization().getNamespace(), "my-namespace");
  }

  @Test
  public void testToAuthorizationConfigurationApi() {
    final AuthorizationConfigurationDTO dto = new AuthorizationConfigurationDTO();
    dto.setNamespace("my-namespace");

    final AuthorizationConfigurationApi gotApi = ApiBeanMapper.toApi(dto);
    assertEquals(gotApi.getNamespace(), "my-namespace");
  }

  @Test
  public void testToAuthorizationConfigurationDTO() {
    final AuthorizationConfigurationApi api = new AuthorizationConfigurationApi()
        .setNamespace("my-namespace");

    final AuthorizationConfigurationDTO gotDto = ApiBeanMapper.toAuthorizationConfigurationDTO(api);
    assertEquals(gotDto.getNamespace(), "my-namespace");
  }

}
