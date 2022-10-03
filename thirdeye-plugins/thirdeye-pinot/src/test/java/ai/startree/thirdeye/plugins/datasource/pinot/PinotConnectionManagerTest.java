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

package ai.startree.thirdeye.plugins.datasource.pinot;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotConnectionBuilder;
import org.testng.annotations.Test;

public class PinotConnectionManagerTest {

  @Test
  public void testGetCallOnce() {
    final PinotConnectionBuilder pinotConnectionBuilder = mock(PinotConnectionBuilder.class);
    final PinotThirdEyeDataSourceConfig config = new PinotThirdEyeDataSourceConfig();
    final PinotConnectionManager pinotConnectionManager = new PinotConnectionManager(
        pinotConnectionBuilder, config, new PinotOauthTokenSupplier(config));

    final Connection connection = mock(Connection.class);
    when(pinotConnectionBuilder.createConnection(any(PinotThirdEyeDataSourceConfig.class)))
        .thenReturn(connection);

    assertThat(pinotConnectionManager.get()).isEqualTo(connection);

    /* Call second time */
    assertThat(pinotConnectionManager.get()).isEqualTo(connection);

    /* Create should be called once */
    verify(pinotConnectionBuilder, times(1))
        .createConnection(any(PinotThirdEyeDataSourceConfig.class));
  }

  @Test
  public void testGetWithOauthWithRenew() throws IOException {
    final File file = createTemporaryTokenFile("sampleToken");

    final var config = new PinotThirdEyeDataSourceConfig()
        .setControllerHost("localhost")
        .setClusterName("name")
        .setControllerConnectionScheme("http")
        .setOauth(new PinotOauthConfiguration()
            .setEnabled(true)
            .setTokenFilePath(file.getAbsolutePath())
        );
    final PinotConnectionBuilder pinotConnectionBuilder = mock(PinotConnectionBuilder.class);
    final PinotConnectionManager pinotConnectionManager = new PinotConnectionManager(
        pinotConnectionBuilder, config, new PinotOauthTokenSupplier(config));

    final Connection connection = mock(Connection.class);
    when(pinotConnectionBuilder.createConnection(any(PinotThirdEyeDataSourceConfig.class)))
        .thenReturn(connection);

    assertThat(pinotConnectionManager.get()).isEqualTo(connection);

    /* Call second time */
    assertThat(pinotConnectionManager.get()).isEqualTo(connection);

    /* Create should be called once */
    verify(pinotConnectionBuilder, times(1))
        .createConnection(any(PinotThirdEyeDataSourceConfig.class));


    writeToken(file, "newToken");

    final Connection newConnection = mock(Connection.class);
    assertThat(newConnection).isNotEqualTo(connection);
    when(pinotConnectionBuilder.createConnection(any(PinotThirdEyeDataSourceConfig.class)))
        .thenReturn(newConnection);

    /* Call third time */
    final Connection actual = pinotConnectionManager.get();
    assertThat(actual).isEqualTo(newConnection);
    /* Create should be called twice */
    verify(pinotConnectionBuilder, times(2))
        .createConnection(any(PinotThirdEyeDataSourceConfig.class));

    verify(connection, timeout(1000)).close();
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
