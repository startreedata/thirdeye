package ai.startree.thirdeye.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataSourceResourceTest {

  private final String dataSourceName = "test";
  private ThirdEyeDataSource dataSource;
  private DataSourceCache dataSourceCache;
  private DataSourceResource dataSourceResource;

  @BeforeMethod
  void setup() {
    this.dataSource = mock(ThirdEyeDataSource.class);
    this.dataSourceCache = mock(DataSourceCache.class);
    this.dataSourceResource = new DataSourceResource(mock(
        DataSourceManager.class), dataSourceCache);
  }

  @Test
  public void statusHealthyTest() {
    when(dataSource.validate()).thenReturn(true);
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(dataSource);
    StatusApi response = (StatusApi) dataSourceResource.status(dataSourceName)
        .getEntity();
    Assert.assertNotNull(response);
    Assert.assertEquals(response.getCode(), ThirdEyeStatus.HEALTHY);
  }

  @Test
  public void validationFailureTest() {
    when(dataSource.validate()).thenReturn(false);
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(dataSource);
    Assert.expectThrows(InternalServerErrorException.class, () -> dataSourceResource.status(dataSourceName));
  }

  @Test
  public void dataSourceNotFoundTest() {
    when(dataSourceCache.getDataSource(dataSourceName)).thenReturn(null);
    Assert.expectThrows(InternalServerErrorException.class, () -> dataSourceResource.status(dataSourceName));
  }

  @Test
  public void dataSourceExceptionTest() {
    when(dataSourceCache.getDataSource(dataSourceName)).thenThrow(new ThirdEyeException(
        ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, dataSourceName));
    Assert.expectThrows(BadRequestException.class, () -> dataSourceResource.status(dataSourceName));
  }
}
