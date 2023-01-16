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
package ai.startree.thirdeye.plugins.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class DurationAnomalyFilterSpec extends AbstractSpec {

  private String minDuration = "PT0S"; // default value 0 seconds
  private String maxDuration = "P365D"; // default value 1 year

  public String getMinDuration() {
    return minDuration;
  }

  public void setMinDuration(String minDuration) {
    this.minDuration = minDuration;
  }

  public String getMaxDuration() {
    return maxDuration;
  }

  public void setMaxDuration(String maxDuration) {
    this.maxDuration = maxDuration;
  }
}
