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
package ai.startree.thirdeye.plugins.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteHttpDetectorSpec extends AbstractSpec {

  private String url;
  private Map<String, Object> params;

  public String getUrl() {
    return url;
  }

  public RemoteHttpDetectorSpec setUrl(final String url) {
    this.url = url;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public RemoteHttpDetectorSpec setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }
}
