package ai.startree.thirdeye.spi.util;

import java.util.Collection;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ListUtils {

  public static <E> boolean isNotEmpty(@NonNull Collection<E> collection) {
    Objects.requireNonNull(collection);
    return !collection.isEmpty();
  }
}
