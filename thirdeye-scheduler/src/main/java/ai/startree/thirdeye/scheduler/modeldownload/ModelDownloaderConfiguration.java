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
package ai.startree.thirdeye.scheduler.modeldownload;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.HashMap;
import java.util.Map;

public class ModelDownloaderConfiguration {

  private TimeGranularity runFrequency;
  private String className;
  private String destinationPath;
  private Map<String, Object> properties = new HashMap<>();

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public TimeGranularity getRunFrequency() {
    return runFrequency;
  }

  public void setRunFrequency(TimeGranularity runFrequency) {
    this.runFrequency = runFrequency;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }
}
