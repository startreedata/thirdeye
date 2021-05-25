package org.apache.pinot.thirdeye.exampleplugin;

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
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    try {
      final Class<?> bClass = Class.forName("org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig");
      System.out.println(bClass);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    final Class<?> cClass;
    try {
      cClass = Class.forName("org.apache.pinot.thirdeye.datalayer.util.NonExistentClass");
      System.out.println(cClass);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
