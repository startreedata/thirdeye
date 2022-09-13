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

package ai.startree.thirdeye.detectionpipeline.operator;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.common.base.Joiner;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumerationItemNameGenerator {

  private static final Logger log = LoggerFactory.getLogger(EnumerationItemNameGenerator.class);

  public String generateName(EnumerationItemDTO enumerationItem) {
    final Map<String, Object> params = requireNonNull(enumerationItem.getParams(),
        "params null in enumeration items");
    return Joiner.on(",").withKeyValueSeparator("=").join(params);
  }
}
