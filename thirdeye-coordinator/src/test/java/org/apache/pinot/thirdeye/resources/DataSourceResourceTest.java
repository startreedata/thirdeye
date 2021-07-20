package org.apache.pinot.thirdeye.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.ThirdEyeException;
import org.apache.pinot.thirdeye.spi.ThirdEyeStatus;
import org.apache.pinot.thirdeye.spi.api.HealthCheckApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DataSourceManager;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataSourceResourceTest {

  private String dataSourceName = "test";

  @Test
  public void statusHealthyTest() {
    ThirdEyeDataSource dataSource = mock(ThirdEyeDataSource.class);
    when(dataSource.validate()).thenReturn(true);
    DataSourceCache dataSourceCache = mock(DataSourceCache.class);
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(dataSource);
    DataSourceResource dataSourceResource = new DataSourceResource(mock(AuthService.class), mock(
        DataSourceManager.class), dataSourceCache);
    Response response = dataSourceResource.status(null, dataSourceName);
    Assert.assertNotNull(response.getEntity());
    Assert.assertTrue(((HealthCheckApi) response.getEntity()).isHealthy());
  }

  @Test
  public void validationFailureTest() {
    ThirdEyeDataSource dataSource = mock(ThirdEyeDataSource.class);
    when(dataSource.validate()).thenReturn(false);
    DataSourceCache dataSourceCache = mock(DataSourceCache.class);
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(dataSource);
    DataSourceResource dataSourceResource = new DataSourceResource(mock(AuthService.class), mock(
        DataSourceManager.class), dataSourceCache);
    Response response = dataSourceResource.status(null, dataSourceName);
    HealthCheckApi respEntity = (HealthCheckApi) response.getEntity();
    Assert.assertNotNull(respEntity);
    Assert.assertFalse(respEntity.isHealthy());
    Assert.assertEquals(respEntity.getMessage(),
        ThirdEyeStatus.ERR_DATASOURCE_INVALID.getMessage());
  }

  @Test
  public void dataSourceNotFoundTest() {
    DataSourceCache dataSourceCache = mock(DataSourceCache.class);
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(null);
    DataSourceResource dataSourceResource = new DataSourceResource(mock(AuthService.class), mock(
        DataSourceManager.class), dataSourceCache);
    Response response = dataSourceResource.status(null, dataSourceName);
    HealthCheckApi respEntity = (HealthCheckApi) response.getEntity();
    Assert.assertNotNull(respEntity);
    Assert.assertFalse(respEntity.isHealthy());
    Assert.assertEquals(respEntity.getMessage(),
        String.format(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND.getMessage(), dataSourceName));
  }

  @Test
  public void dataSourceExceptionTest() {
    DataSourceCache dataSourceCache = mock(DataSourceCache.class);
    when(dataSourceCache.getDataSource(dataSourceName)).thenThrow(new ThirdEyeException(
        ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, dataSourceName));
    DataSourceResource dataSourceResource = new DataSourceResource(mock(AuthService.class), mock(
        DataSourceManager.class), dataSourceCache);
    Response response = dataSourceResource.status(null, dataSourceName);
    HealthCheckApi respEntity = (HealthCheckApi) response.getEntity();
    Assert.assertNotNull(respEntity);
    Assert.assertFalse(respEntity.isHealthy());
    Assert.assertEquals(respEntity.getMessage(),
        String.format(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND.getMessage(), dataSourceName));
  }
}
