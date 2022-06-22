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

import java.util.Objects;

// TODO prefer Predicate in all ThirdEye - see buildFilter in CrudResource
@Deprecated
public final class FilterPredicate {

  final String key;
  final String operator;
  final String value;

  public FilterPredicate(String key, String operator, String value) {
    this.key = key;
    this.operator = operator;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FilterPredicate that = (FilterPredicate) o;
    return Objects.equals(key, that.key) && Objects.equals(operator, that.operator) && Objects
        .equals(value,
            that.value);
  }

  @Override
  public int hashCode() {

    return Objects.hash(key, operator, value);
  }

  public String getKey() {
    return key;
  }

  public String getOperator() {
    return operator;
  }

  public String getValue() {
    return value;
  }
}
