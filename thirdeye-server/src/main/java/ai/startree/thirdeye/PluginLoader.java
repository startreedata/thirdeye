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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.auth.AccessControlProvider;
import ai.startree.thirdeye.core.BootstrapResourcesRegistry;
import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.rootcause.ContributorsFinderRunner;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.PluginClassLoader;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProviderFactory;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactory;
import ai.startree.thirdeye.spi.detection.EnumeratorFactory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads all plugins from a plugins dir as configured in {@link PluginLoaderConfiguration}
 * Expected Directory Structure:
 * - plugins/
 * -        /exampleplugin
 *
 * Plugins can have jars and resource files in the plugin directory which is loaded using
 * a {@link URLClassLoader} using the {@link ServiceLoader} interface.
 */
@Singleton
public class PluginLoader {

  private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

  private final DataSourcesLoader dataSourcesLoader;
  private final DetectionRegistry detectionRegistry;
  private final NotificationServiceRegistry notificationServiceRegistry;
  private final ContributorsFinderRunner contributorsFinderRunner;
  private final BootstrapResourcesRegistry bootstrapResourcesRegistry;
  private final PostProcessorRegistry postProcessorRegistry;
  private final AccessControlProvider accessControlProvider;

  private final AtomicBoolean loading = new AtomicBoolean();
  private final File pluginsDir;

  @Inject
  public PluginLoader(
      final DataSourcesLoader dataSourcesLoader,
      final DetectionRegistry detectionRegistry,
      final NotificationServiceRegistry notificationServiceRegistry,
      final ContributorsFinderRunner contributorsFinderRunner,
      final BootstrapResourcesRegistry bootstrapResourcesRegistry,
      final PostProcessorRegistry postProcessorRegistry,
      final AccessControlProvider accessControlProvider,
      final PluginLoaderConfiguration config) {
    this.dataSourcesLoader = dataSourcesLoader;
    this.detectionRegistry = detectionRegistry;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.contributorsFinderRunner = contributorsFinderRunner;
    this.bootstrapResourcesRegistry = bootstrapResourcesRegistry;
    this.postProcessorRegistry = postProcessorRegistry;
    this.accessControlProvider = accessControlProvider;
    pluginsDir = new File(config.getPluginsPath());
  }

  public void loadPlugins() {
    if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
      log.error("Skipping Plugin Loading. Plugin dir not found: " + pluginsDir);
      return;
    }

    if (loading.compareAndSet(false, true)) {
      final File[] files = requireNonNull(pluginsDir.listFiles());
      for (File pluginDir : files) {
        if (pluginDir.isDirectory()) {
          loadPlugin(pluginDir);
        }
      }
    }
  }

  private void loadPlugin(final File pluginDir) {
    log.info("Loading plugin: " + pluginDir);
    final URLClassLoader pluginClassLoader = createPluginClassLoader(pluginDir);
    final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(pluginClassLoader);
      for (Plugin plugin : ServiceLoader.load(Plugin.class, pluginClassLoader)) {
        installPlugin(plugin);
      }
    } finally {
      Thread.currentThread().setContextClassLoader(currentClassLoader);
    }
  }

  private void installPlugin(final Plugin plugin) {
    log.info("Installing plugin: " + plugin.getClass().getName());
    for (ThirdEyeDataSourceFactory f : plugin.getDataSourceFactories()) {
      dataSourcesLoader.addThirdEyeDataSourceFactory(f);
    }
    for (AnomalyDetectorFactory f : plugin.getAnomalyDetectorFactories()) {
      detectionRegistry.addAnomalyDetectorFactory(f);
    }
    for (EventTriggerFactory f : plugin.getEventTriggerFactories()) {
      detectionRegistry.addEventTriggerFactory(f);
    }
    for (NotificationServiceFactory f : plugin.getNotificationServiceFactories()) {
      notificationServiceRegistry.addNotificationServiceFactory(f);
    }
    for (ContributorsFinderFactory f: plugin.getContributorsFinderFactories()) {
      contributorsFinderRunner.addContributorsFinderFactory(f);
    }
    for (BootstrapResourcesProviderFactory f: plugin.getBootstrapResourcesProviderFactories()) {
      bootstrapResourcesRegistry.addBootstrapResourcesProviderFactory(f);
    }
    for (AnomalyPostProcessorFactory f: plugin.getAnomalyPostProcessorFactories()) {
      postProcessorRegistry.addAnomalyPostProcessorFactory(f);
    }
    for (EnumeratorFactory f : plugin.getEnumeratorFactories()) {
      detectionRegistry.addEnumeratorFactory(f);
    }
    if (accessControlProvider.getConfig().enabled) {
      optional(plugin.getAccessControl(accessControlProvider.getConfig()))
          .ifPresent(accessControlProvider::setAccessControl);
    }

    log.info("Installed plugin: " + plugin.getClass().getName());
  }

  private URLClassLoader createPluginClassLoader(File dir) {
    final URL[] urls = Arrays.stream(optional(dir.listFiles()).orElse(new File[]{}))
        .sorted()
        .map(File::toURI)
        .map(this::toUrl)
        .toArray(URL[]::new);

    return new PluginClassLoader(urls, getClass().getClassLoader());
  }

  private URL toUrl(final URI uri) {
    try {
      return uri.toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
