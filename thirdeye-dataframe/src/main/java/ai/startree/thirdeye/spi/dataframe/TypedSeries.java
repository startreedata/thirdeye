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
package ai.startree.thirdeye.spi.dataframe;

@SuppressWarnings("unchecked")
public abstract class TypedSeries<T extends Series> extends Series {

  @Override
  public T head(int n) {
    return (T) super.head(n);
  }

  @Override
  public T tail(int n) {
    return (T) super.tail(n);
  }

  @Override
  public T sliceFrom(int from) {
    return (T) super.sliceFrom(from);
  }

  @Override
  public T sliceTo(int to) {
    return (T) super.sliceTo(to);
  }

  @Override
  public T reverse() {
    return (T) super.reverse();
  }

  @Override
  public T unique() {
    return (T) super.unique();
  }

  @Override
  public T copy() {
    return (T) super.copy();
  }

  @Override
  public T append(Series... other) {
    return (T) super.append(other);
  }

  @Override
  public T fillNullForward() {
    return (T) super.fillNullForward();
  }

  @Override
  public T fillNullBackward() {
    return (T) super.fillNullBackward();
  }

  @Override
  public T dropNull() {
    return (T) super.dropNull();
  }

  @Override
  public T filter(Conditional conditional) {
    return (T) super.filter(conditional);
  }

  @Override
  public T first() {
    return (T) super.first();
  }

  @Override
  public T last() {
    return (T) super.last();
  }

  @Override
  public T set(Series other) {
    return (T) super.set(other);
  }
}
