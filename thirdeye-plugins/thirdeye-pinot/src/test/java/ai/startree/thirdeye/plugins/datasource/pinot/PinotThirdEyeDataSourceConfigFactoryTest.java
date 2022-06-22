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

import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfigFactory;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfigFactory.Builder;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdeyeDataSourceProperties;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PinotThirdEyeDataSourceConfigFactoryTest {

  private static final String CONTROLLER_HOST = "host";
  private static final String CONTROLLER_PORT = "1234";
  private static final String ZOOKEEPER_URL = "zookeeper";
  private static final String CLUSTER_NAME = "clusterName";
  private static final String BROKER_URL = "brokerURL";
  private static final String TAG = "tag";
  private static final String SPACE_STRING = "      ";

  ImmutableMap<String, Object> processedProperties;

  @Test
  public void testCreateProcessedPropertyMap() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(PinotThirdeyeDataSourceProperties.CONTROLLER_HOST.getValue(),
        SPACE_STRING + CONTROLLER_HOST);
    properties.put(PinotThirdeyeDataSourceProperties.CONTROLLER_PORT.getValue(),
        CONTROLLER_PORT + SPACE_STRING);
    properties.put(PinotThirdeyeDataSourceProperties.ZOOKEEPER_URL.getValue(),
        ZOOKEEPER_URL + SPACE_STRING);
    properties.put(PinotThirdeyeDataSourceProperties.CLUSTER_NAME.getValue(),
        SPACE_STRING + CLUSTER_NAME);
    properties
        .put(PinotThirdeyeDataSourceProperties.BROKER_URL.getValue(), SPACE_STRING + BROKER_URL);
    properties.put(PinotThirdeyeDataSourceProperties.TAG.getValue(), TAG + SPACE_STRING);

    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    builder.put(PinotThirdeyeDataSourceProperties.CONTROLLER_HOST.getValue(), CONTROLLER_HOST);
    builder.put(PinotThirdeyeDataSourceProperties.CONTROLLER_PORT.getValue(), CONTROLLER_PORT);
    builder.put(PinotThirdeyeDataSourceProperties.ZOOKEEPER_URL.getValue(), ZOOKEEPER_URL);
    builder.put(PinotThirdeyeDataSourceProperties.CLUSTER_NAME.getValue(), CLUSTER_NAME);
    builder.put(PinotThirdeyeDataSourceProperties.BROKER_URL.getValue(), BROKER_URL);
    builder.put(PinotThirdeyeDataSourceProperties.TAG.getValue(), TAG);
    ImmutableMap<String, Object> expectedProperties = builder.build();

    processedProperties = PinotThirdEyeDataSourceConfigFactory.processPropertyMap(properties);

    Assert.assertEquals(processedProperties, expectedProperties);
  }

  @Test
  public void testCreateProcessedPropertyMapWithEmptyMap() throws Exception {
    Map<String, Object> properties = new HashMap<>();

    ImmutableMap<String, Object> processPropertyMap = PinotThirdEyeDataSourceConfigFactory
        .processPropertyMap(properties);

    Assert.assertNull(processPropertyMap);
  }

  @Test
  public void testCreateProcessedPropertyMapWithNullMap() throws Exception {
    ImmutableMap<String, Object> processPropertyMap = PinotThirdEyeDataSourceConfigFactory
        .processPropertyMap(null);

    Assert.assertNull(processPropertyMap);
  }

  @Test
  public void testProcessPropertyMapWithMissingProperties() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(PinotThirdeyeDataSourceProperties.CONTROLLER_HOST.getValue(),
        SPACE_STRING + CONTROLLER_HOST);
    properties.put(PinotThirdeyeDataSourceProperties.CONTROLLER_PORT.getValue(),
        CONTROLLER_PORT + SPACE_STRING);
    // Missing ZOOKEEPER_URL
    properties.put(PinotThirdeyeDataSourceProperties.CLUSTER_NAME.getValue(),
        SPACE_STRING + CLUSTER_NAME);
    // Returned a null property map
    ImmutableMap<String, Object> processPropertyMap = PinotThirdEyeDataSourceConfigFactory
        .processPropertyMap(properties);

    Assert.assertNull(processPropertyMap);
  }

  @Test(dependsOnMethods = "testCreateProcessedPropertyMap")
  public void testCreateFromProperties() throws Exception {
    Builder builder =
        PinotThirdEyeDataSourceConfigFactory.builder().setControllerHost(CONTROLLER_HOST)
            .setControllerPort(Integer.parseInt(CONTROLLER_PORT)).setZookeeperUrl(ZOOKEEPER_URL)
            .setClusterName(CLUSTER_NAME).setBrokerUrl(BROKER_URL).setTag(TAG);

    PinotThirdEyeDataSourceConfig expectedDataSourceConfig = builder.build();

    PinotThirdEyeDataSourceConfig actualDataSourceConfig =
        PinotThirdEyeDataSourceConfigFactory.createFromProperties(processedProperties);

    Assert.assertEquals(actualDataSourceConfig, expectedDataSourceConfig);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testBuilderWithIllegalArgument() throws Exception {
    Builder builder =
        PinotThirdEyeDataSourceConfigFactory.builder().setControllerHost(CONTROLLER_HOST)
            .setZookeeperUrl(ZOOKEEPER_URL)
            .setClusterName(CLUSTER_NAME);

    builder.build();
  }

  @Test(expectedExceptions = {NullPointerException.class})
  public void testBuilderWithNullArgument() throws Exception {
    Builder builder =
        PinotThirdEyeDataSourceConfigFactory.builder().setControllerHost(CONTROLLER_HOST)
            .setControllerPort(Integer.parseInt(CONTROLLER_PORT)).setClusterName(CLUSTER_NAME);

    builder.build();
  }
}
