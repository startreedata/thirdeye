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
package ai.startree.thirdeye.spi.datasource.macro;

import java.util.List;

public interface MacroFunction {

  String AUTO_TIME_CONFIG = "AUTO";

  String name();

  String expandMacro(List<String> macroParams, MacroFunctionContext context);

  default boolean isAutoTimeConfiguration(String timeColumn) {
    return timeColumn.equals(AUTO_TIME_CONFIG) || timeColumn.substring(1, timeColumn.length()-1).equals(AUTO_TIME_CONFIG);
  }
}
