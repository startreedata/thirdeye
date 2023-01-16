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
package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.Map;
import java.util.StringJoiner;

public class NotificationSpecDTO {

  private String type;
  private Map<String, Object> params;

  public String getType() {
    return type;
  }

  public NotificationSpecDTO setType(final String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public NotificationSpecDTO setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", NotificationSpecDTO.class.getSimpleName() + "[", "]")
        .add("type='" + type + "'")
        .add("params=" + params)
        .toString();
  }
}
