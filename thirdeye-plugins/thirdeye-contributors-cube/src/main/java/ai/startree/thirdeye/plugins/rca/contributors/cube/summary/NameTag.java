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

package ai.startree.thirdeye.plugins.rca.contributors.cube.summary;

import static ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi.ALL;
import static ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi.ALL_OTHERS;
import static ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi.EMPTY;

import ai.startree.thirdeye.plugins.rca.contributors.cube.data.DimensionValues;
import java.util.ArrayList;
import java.util.List;

public class NameTag {

  public final List<String> names;

  NameTag(int levelCount) {
    names = new ArrayList<>(levelCount);
    for (int i = 0; i < levelCount; ++i) {
      names.add(ALL);
    }
  }

  public List<String> getNames() {
    return names;
  }

  void copyNames(DimensionValues dimensionValues) {
    for (int i = 0; i < dimensionValues.size(); ++i) {
      names.set(i, dimensionValues.get(i));
    }
  }

  void setAllOthers(int index) {
    names.set(index, ALL_OTHERS);
  }

  void setEmpty(int index) {
    names.set(index, EMPTY);
  }
}
