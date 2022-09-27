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

package ai.startree.thirdeye.plugins.enumerator;

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.Enumerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class DefaultEnumerator implements Enumerator {

  @Override
  public List<EnumerationItemDTO> enumerate(final Context context) {
    final var params = new ObjectMapper().convertValue(context.getParams(),
        DefaultEnumeratorParams.class);

    return params.getItems();
  }
}
