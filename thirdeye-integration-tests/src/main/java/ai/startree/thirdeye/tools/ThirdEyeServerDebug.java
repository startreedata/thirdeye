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
package ai.startree.thirdeye.tools;

import static ai.startree.thirdeye.ServerUtils.logJvmSettings;

import ai.startree.thirdeye.PluginLoader;
import ai.startree.thirdeye.ThirdEyeServer;
import ai.startree.thirdeye.plugins.bootstrap.opencore.OpenCoreBoostrapResourcesProviderPlugin;
import ai.startree.thirdeye.plugins.datasource.DefaultDataSourcesPlugin;
import ai.startree.thirdeye.plugins.datasource.PinotDataSourcePlugin;
import ai.startree.thirdeye.plugins.detection.components.DetectionComponentsPlugin;
import ai.startree.thirdeye.plugins.detectors.DetectorsPlugin;
import ai.startree.thirdeye.plugins.enumerator.ThirdEyeEnumeratorsPlugin;
import ai.startree.thirdeye.plugins.notification.email.EmailNotificationPlugin;
import ai.startree.thirdeye.plugins.postprocessor.PostProcessorsPlugin;
import ai.startree.thirdeye.plugins.rca.contributors.simple.SimpleContributorsFinderPlugin;
import ai.startree.thirdeye.spi.Plugin;
import com.google.inject.Injector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeServerDebug {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeServerDebug.class);

  public static void main(String[] args) throws Exception {
    logJvmSettings();

    final ThirdEyeServer thirdEyeServer = new ThirdEyeServer();
    thirdEyeServer.run(args);

    final Injector injector = thirdEyeServer.getInjector();

    final PluginLoader pluginLoader = injector.getInstance(PluginLoader.class);
    Stream.of(
            new OpenCoreBoostrapResourcesProviderPlugin(),
            new SimpleContributorsFinderPlugin(),
            new DefaultDataSourcesPlugin(),
            new DetectionComponentsPlugin(),
            new DetectorsPlugin(),
            new EmailNotificationPlugin(),
            new PinotDataSourcePlugin(),
            new ThirdEyeEnumeratorsPlugin(),
            new PostProcessorsPlugin()
        )
        .forEach(plugin -> installPlugin(pluginLoader, plugin));
  }

  /**
   * invoke private method in PluginLoader.installPlugin(Plugin plugin)
   */
  private static void installPlugin(final PluginLoader pluginLoader,
      final Plugin plugin) {
    try {
      final Method method = pluginLoader.getClass()
          .getDeclaredMethod("installPlugin", Plugin.class);
      method.setAccessible(true);
      method.invoke(pluginLoader, plugin);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
