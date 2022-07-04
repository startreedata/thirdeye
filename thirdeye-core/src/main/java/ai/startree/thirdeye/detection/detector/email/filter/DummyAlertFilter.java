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
package ai.startree.thirdeye.detection.detector.email.filter;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DummyAlertFilter extends BaseAlertFilter {

  @Override
  public List<String> getPropertyNames() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public void setParameters(Map<String, String> props) {
    // Does nothing
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    return true;
  }

  @Override
  public String toString() {
    return "DummyFilter";
  }
}
