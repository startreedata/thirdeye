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

package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.List;
import java.util.Objects;

public class EventContextDto {

  private List<String> types;
  private String sqlFilter;

  public List<String> getTypes() {
    return types;
  }

  public EventContextDto setTypes(final List<String> types) {
    this.types = types;
    return this;
  }

  public String getSqlFilter() {
    return sqlFilter;
  }

  public EventContextDto setSqlFilter(final String sqlFilter) {
    this.sqlFilter = sqlFilter;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final EventContextDto that = (EventContextDto) o;
    return Objects.equals(types, that.types) && Objects.equals(sqlFilter,
        that.sqlFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(types, sqlFilter);
  }
}
