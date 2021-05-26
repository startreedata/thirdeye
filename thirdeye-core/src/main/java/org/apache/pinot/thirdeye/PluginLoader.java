package org.apache.pinot.thirdeye;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

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
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.PluginClassLoader;
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

  private final AtomicBoolean loading = new AtomicBoolean();
  private final File pluginsDir;

  @Inject
  public PluginLoader(PluginLoaderConfiguration config) {
    pluginsDir = new File(config.getPluginsPath());
  }

  /**
   * TODO spyne remove temporary code.
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    new PluginLoader(
        new PluginLoaderConfiguration()
            .setPluginsPath("/Users/spyne/repo/thirdeye/thirdeye-example-plugin/target/plugins")
    ).loadPlugins();
  }

  public void loadPlugins() throws Exception {
    if (loading.compareAndSet(false, true)) {
      checkArgument(pluginsDir.exists() && pluginsDir.isDirectory());

      final File[] files = requireNonNull(pluginsDir.listFiles());
      for (File pluginDir : files) {
        if (pluginDir.isDirectory()) {
          loadPlugin(pluginDir);
        }
      }
    }
  }

  private void loadPlugin(final File pluginDir) throws Exception {
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
  }

  private URLClassLoader createPluginClassLoader(File dir) {
    final URL[] urls = Arrays.stream(optional(dir.listFiles()).orElse(new File[]{}))
        .map(File::toURI)
        .map(this::toUrl)
        .sorted()
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
