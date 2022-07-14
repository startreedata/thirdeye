package ai.startree.thirdeye.spi.util;

import java.util.Collection;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ListUtils {

  public static <E> boolean isNotEmpty(@NonNull Collection<E> collection) {
    Objects.requireNonNull(collection);
    return !collection.isEmpty();
  }

  public static <E> boolean isNullOrEmpty(@Nullable Collection<E> collection) {
    return collection == null || collection.isEmpty();
  }
}
