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
package ai.startree.thirdeye.plugins.exampleplugin;

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.api.AlertApi;
import com.google.auto.service.AutoService;

@AutoService(Plugin.class)
public class ExamplePlugin implements Plugin {

  public ExamplePlugin() {
    System.out.println("example plugin loaded");
    testAvailableClasses();
  }

  private void testAvailableClasses() {
    try {
      final Class<?> aClass = Class.forName("ai.startree.thirdeye.spi.api.AlertApi");
      final AlertApi api = (AlertApi) aClass.newInstance();
      System.out.println(api.setName("check").getName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }

    checkClass("ai.startree.thirdeye.datalayer.util.DatabaseConfiguration");
    checkClass("ai.startree.thirdeye.datalayer.util.NonExistentClass");
  }

  private void checkClass(final String classRef) {
    System.out.printf("Class: %s isLoadable: %s %n", classRef, isLoadable(classRef));
  }

  private boolean isLoadable(final String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
