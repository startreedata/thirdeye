/*
 * Copyright 2022 StarTree Inc
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
