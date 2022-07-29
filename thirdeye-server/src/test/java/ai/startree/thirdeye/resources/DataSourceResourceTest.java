/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.resources;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import javax.ws.rs.core.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataSourceResourceTest {

  private final String dataSourceName = "test";
  private final ThirdEyePrincipal principal = new ThirdEyePrincipal("test");
  private ThirdEyeDataSource dataSource;
  private DataSourceCache dataSourceCache;
  private DataSourceResource dataSourceResource;

  @BeforeMethod
  void setup() {
    dataSource = mock(ThirdEyeDataSource.class);
    dataSourceCache = mock(DataSourceCache.class);
    dataSourceResource = new DataSourceResource(mock(DataSourceManager.class), dataSourceCache);
  }

  @Test
  public void testValidateOk() {
    when(dataSource.validate()).thenReturn(true);
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(dataSource);

    final Response response = dataSourceResource.validate(principal, dataSourceName);
    assertThat(response.getStatus()).isEqualTo(200);

    final StatusApi entity = (StatusApi) response.getEntity();
    assertThat(entity).isNotNull();
    assertThat(entity.getCode()).isEqualTo(ThirdEyeStatus.OK);
  }

  @Test
  public void testValidateFailure() {
    when(dataSource.validate()).thenReturn(false);
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(dataSource);
    final Response response = dataSourceResource.validate(principal, dataSourceName);
    assertThat(response.getStatus()).isEqualTo(200);

    final StatusListApi entity = (StatusListApi) response.getEntity();
    assertThat(entity.getList()).isNotNull();
    assertThat(entity.getList().isEmpty()).isFalse();

    final StatusApi statusApi = entity.getList().get(0);
    assertThat(statusApi.getCode()).isEqualTo(ThirdEyeStatus.ERR_DATASOURCE_VALIDATION_FAILED);
  }
}
