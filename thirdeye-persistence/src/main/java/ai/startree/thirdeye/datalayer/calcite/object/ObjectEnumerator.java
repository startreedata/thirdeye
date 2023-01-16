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
package ai.startree.thirdeye.datalayer.calcite.object;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Nullness;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ObjectEnumerator<T> implements Enumerator<Object[]> {

  private final List<T> elements;
  private final AtomicBoolean cancelFlag;
  private final ObjectToRelationAdapter<T> adapter;
  private @Nullable Object[] current;
  private int currentIndex = -1;

  public ObjectEnumerator(final List<T> elements, final AtomicBoolean cancelFlag, final ObjectToRelationAdapter<T> adapter) {
    this.cancelFlag = cancelFlag;
    this.elements = elements;
    this.adapter = adapter;
  }

  @Override
  public Object[] current() {
    return Nullness.castNonNull(current);
  }

  @Override
  public boolean moveNext() {
    for (; ; ) {
      if (cancelFlag.get()) {
        return false;
      }
      currentIndex++;
      if (currentIndex == elements.size()) {
        return false;
      }

      final T currentElement = elements.get(currentIndex);
      current = adapter.getRow(currentElement);
      return true;
    }
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    //nothing to do
  }


}
