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
package ai.startree.thirdeye;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.resources.RootResource;
import ai.startree.thirdeye.worker.task.TaskDriverConfiguration;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

public class ThirdEyeServerModuleTest {

  private static DataSource mockDataSource() throws SQLException {
    final ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenAnswer(new Answer<Boolean>() {
      private boolean value = false;

      @Override
      public Boolean answer(final InvocationOnMock invocationOnMock) {
        /* Alternate between true and false */
        value = !value;
        return value;
      }
    });
    when(resultSet.getString(anyInt())).thenReturn("string");

    final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
    when(databaseMetaData.getColumns(any(), any(), any(), any()))
        .thenReturn(resultSet);

    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenReturn(databaseMetaData);
    when(connection.getCatalog()).thenReturn("thirdeye-test-db");

    /* Add a back reference as well */
    when(databaseMetaData.getConnection()).thenReturn(connection);

    final DataSource dataSource = mock(DataSource.class);
    when(dataSource.getConnection()).thenReturn(connection);
    return dataSource;
  }

  @Test
  public void testRootResourceInjection() throws Exception {
    final DataSource dataSource = mockDataSource();

    final ThirdEyeServerConfiguration configuration = new ThirdEyeServerConfiguration()
        .setAuthConfiguration(new AuthConfiguration())
        .setTaskDriverConfiguration(new TaskDriverConfiguration().setId(0L));

    final Injector injector = Guice.createInjector(new ThirdEyeServerModule(
        configuration,
        dataSource,
        new MetricRegistry()));

    assertThat(injector.getInstance(RootResource.class)).isNotNull();
  }
}
