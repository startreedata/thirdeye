package org.apache.pinot.thirdeye.plugin.exampleplugin;

import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.api.AlertApi;

public class ExamplePlugin implements Plugin {

  public ExamplePlugin() {
    System.out.println("example plugin loaded");
    testAvailableClasses();
  }

  private void testAvailableClasses() {
    try {
      final Class<?> aClass = Class.forName("org.apache.pinot.thirdeye.spi.api.AlertApi");
      final AlertApi api = (AlertApi) aClass.newInstance();
      System.out.println(api.setName("check").getName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }

    checkClass("org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration");
    checkClass("org.apache.pinot.thirdeye.datalayer.util.NonExistentClass");
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
