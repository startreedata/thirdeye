package ai.startree.thirdeye.plugin.exampleplugin;

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.api.AlertApi;

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
