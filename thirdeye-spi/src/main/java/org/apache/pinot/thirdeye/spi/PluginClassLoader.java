/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.thirdeye.spi;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Loads a plugin leveraging a {@link URLClassLoader}. However, it restricts the plugin from
 * using the system classloader thereby trimming access to all system classes.
 *
 * Only the classes in SHARED_PACKAGES are visible to the plugin.
 *
 */
public class PluginClassLoader extends URLClassLoader {

  private static final ClassLoader PLATFORM_CLASS_LOADER = platformClassLoaderIfExists();

  public static final ImmutableList<String> SHARED_PACKAGES = ImmutableList.<String>builder()
      .add("org.apache.pinot.thirdeye.spi")
      .add("com.google.common")
      .add("org.joda.time")
      .add("org.slf4j")
      .add("org.h2")
      .add("com.mysql")
      .add("javax.activation")
      .build();




  /*
  *
  *
  * .add("org.apache.pinot.thirdeye.spi")
      .add("org.joda.time")
      .add("org.slf4j")
      .add("org.h2")
      .add("com.google.common")
      .add("com.mysql")
      .add("com.sun")
      .add("javax.mail")
  * */
  private final ClassLoader parentClassLoader;

  public PluginClassLoader(final URL[] urls, final ClassLoader parentClassLoader) {
    super(urls, PLATFORM_CLASS_LOADER);
    this.parentClassLoader = parentClassLoader;
  }

  /**
   * For Java 8 or earlier, sending 'null' as the parent classloader works fine for loading
   * classes using the app classloader. For java 9 and above, this is done using a different API.
   *
   * This approach is also used in other codebases.
   * https://github.com/prestodb/presto/blob/master/presto-main/src/main/java/com/facebook/presto/server/PluginClassLoader.java
   *
   * @return platform class loader if available.
   */
  @SuppressWarnings("JavaReflectionMemberAccess")
  private static ClassLoader platformClassLoaderIfExists() {
    try {
      // Return the platform class loader if available
      // For Java 8 and earlier, this method is not available and
      final Method method = ClassLoader.class.getMethod("getPlatformClassLoader");
      return (ClassLoader) method.invoke(null);
    } catch (final NoSuchMethodException ignored) {
      // use null class loader on Java 8
      return null;
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Class<?> loadClass(final String name, final boolean resolve)
      throws ClassNotFoundException {
    // has the class loaded already?
    Class<?> loadedClass = findLoadedClass(name);
    if (loadedClass == null) {
      final boolean isSharedClass = SHARED_PACKAGES.stream().anyMatch(name::startsWith);
      if (isSharedClass) {
        loadedClass = parentClassLoader.loadClass(name);
      } else {
        loadedClass = super.loadClass(name, resolve);
      }
    }

    if (resolve) {      // marked to resolve
      resolveClass(loadedClass);
    }
    return loadedClass;
  }
}
