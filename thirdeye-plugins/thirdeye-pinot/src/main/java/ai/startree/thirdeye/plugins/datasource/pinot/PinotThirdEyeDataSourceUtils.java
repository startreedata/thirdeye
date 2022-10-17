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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;

public class PinotThirdEyeDataSourceUtils {

  public static PinotThirdEyeDataSourceConfig buildConfig(
      final Map<String, Object> properties) {
    final PinotThirdEyeDataSourceConfig config = new ObjectMapper()
        .convertValue(properties, PinotThirdEyeDataSourceConfig.class);

    requireNonNull(config.getControllerHost(), "Controller Host is not set.");
    checkArgument(config.getControllerPort() >= 0, "Controller Portis not set");
    requireNonNull(config.getClusterName(), "Cluster Name is not set.");
    checkArgument(Set.of(PinotThirdEyeDataSource.HTTP_SCHEME, PinotThirdEyeDataSource.HTTPS_SCHEME)
            .contains(config.getControllerConnectionScheme()),
        "Controller scheme must be  either 'http' or 'https'");

    return config;
  }

  public static PinotThirdEyeDataSourceConfig cloneConfig(
      final PinotThirdEyeDataSourceConfig config) {
    final Map<String, Object> map = new ObjectMapper()
        .convertValue(config, new TypeReference<>() {});
    return buildConfig(map);
  }
}
