package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import com.google.inject.Injector;

public class SharedInjector {

  private static final Injector SHARED_INJECTOR = new MySqlTestDatabase().createInjector();

  public static Injector get() {
    return SHARED_INJECTOR;
  }
}
