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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.apache.pinot.client.Connection;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class PinotConnectionUtilsTest {

  @Test
  public void testGetCallOnce() {
    try (MockedStatic<PinotConnectionUtils> mockedConnectionUtils = Mockito.mockStatic(PinotConnectionUtils.class)) {
      final Connection connection = mock(Connection.class);
      mockedConnectionUtils.when(() -> PinotConnectionUtils.createConnection(any(PinotThirdEyeDataSourceConfig.class), 
          any(Map.class))).thenReturn(connection);
      
      final PinotThirdEyeDataSourceConfig config = new PinotThirdEyeDataSourceConfig();
      final PinotConnectionProvider pinotConnectionProvider = new PinotConnectionProvider(config);
      assertThat(pinotConnectionProvider.get()).isEqualTo(connection);
      /* Call second time */
      assertThat(pinotConnectionProvider.get()).isEqualTo(connection);
      /* Create should be called once */
      mockedConnectionUtils.verify(
          () -> PinotConnectionUtils.createConnection(any(PinotThirdEyeDataSourceConfig.class),
              any(Map.class)), 
          times(1)
      );
    }
    
  }

  @Test
  public void testGetWithOauthWithRenew() throws IOException {
    final File file = createTemporaryTokenFile("sampleToken");

    final PinotThirdEyeDataSourceConfig config = new PinotThirdEyeDataSourceConfig()
        .setControllerHost("localhost")
        .setClusterName("name")
        .setControllerConnectionScheme("http")
        .setOauth(new PinotOauthConfiguration()
            .setEnabled(true)
            .setTokenFilePath(file.getAbsolutePath())
        );

    try (MockedStatic<PinotConnectionUtils> mockedConnectionUtils = Mockito.mockStatic(PinotConnectionUtils.class)) {
      final Connection connection = mock(Connection.class);
      mockedConnectionUtils.when(() -> PinotConnectionUtils.createConnection(any(PinotThirdEyeDataSourceConfig.class),
          any(Map.class))).thenReturn(connection);
      
      final PinotConnectionProvider pinotConnectionProvider = new PinotConnectionProvider(config);
      assertThat(pinotConnectionProvider.get()).isEqualTo(connection);
      /* Call second time */
      assertThat(pinotConnectionProvider.get()).isEqualTo(connection);
      /* Create should be called once */
      mockedConnectionUtils.verify(
          () -> PinotConnectionUtils.createConnection(any(PinotThirdEyeDataSourceConfig.class),
              any(Map.class)),
          times(1));

      writeToken(file, "newToken");
      final Connection newConnection = mock(Connection.class);
      assertThat(newConnection).isNotEqualTo(connection);
      mockedConnectionUtils.when(() -> PinotConnectionUtils.createConnection(any(PinotThirdEyeDataSourceConfig.class),
          any(Map.class))).thenReturn(newConnection);
      /* Call third time */
      final Connection actual = pinotConnectionProvider.get();
      assertThat(actual).isEqualTo(newConnection);
      /* Create should be called twice */
      mockedConnectionUtils.verify(
          () -> PinotConnectionUtils.createConnection(any(PinotThirdEyeDataSourceConfig.class),
              any(Map.class)),
          times(2));
    }
  }

  private static File createTemporaryTokenFile(final String token) throws IOException {
    final File file = File.createTempFile("tokenFilePath", "txt");
    writeToken(file, token);
    file.deleteOnExit();
    return file;
  }

  private static void writeToken(final File file, final String token) throws IOException {
    final FileWriter fileWriter = new FileWriter(file, true);

    final BufferedWriter bw = new BufferedWriter(fileWriter);
    bw.write(token);
    bw.close();
  }
}
