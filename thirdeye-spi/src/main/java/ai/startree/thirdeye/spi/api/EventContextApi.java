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

package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class EventContextApi {

  private Templatable<List<String>> types;
  private String sqlFilter;

  public Templatable<List<String>> getTypes() {
    return types;
  }

  public EventContextApi setTypes(final Templatable<List<String>> types) {
    this.types = types;
    return this;
  }

  public String getSqlFilter() {
    return sqlFilter;
  }

  public EventContextApi setSqlFilter(final String sqlFilter) {
    this.sqlFilter = sqlFilter;
    return this;
  }
}
