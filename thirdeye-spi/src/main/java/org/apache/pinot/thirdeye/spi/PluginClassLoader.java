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
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Loads a plugin leveraging a {@link URLClassLoader}. However, it restricts the plugin from
 * using the system classloader thereby trimming access to all system classes.
 *
 * Only the classes in SHARED_PACKAGES are visible to the plugin.
 */
public class PluginClassLoader extends URLClassLoader {

  public static final ImmutableList<String> SHARED_PACKAGES = ImmutableList.<String>builder()
      .add("org.apache.pinot.thirdeye.spi")
      .add("org.apache.pinot.client")
      .add("com.google.common")
      .add("org.joda.time")
      .build();

  private final ClassLoader parentClassLoader;

  public PluginClassLoader(URL[] urls, ClassLoader parentClassLoader) {
    super(urls, null);
    this.parentClassLoader = parentClassLoader;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve)
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
