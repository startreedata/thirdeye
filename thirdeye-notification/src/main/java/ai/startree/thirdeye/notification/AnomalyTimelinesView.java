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
package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.util.TimeBucket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnomalyTimelinesView {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyTimelinesView.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static int DEFAULT_MAX_SIZE = CondensedAnomalyTimelinesView.DEFAULT_MAX_LENGTH;

  List<TimeBucket> timeBuckets = new ArrayList<>();
  Map<String, String> summary = new HashMap<>();
  List<Double> currentValues = new ArrayList<>();
  List<Double> baselineValues = new ArrayList<>();

  public List<TimeBucket> getTimeBuckets() {
    return timeBuckets;
  }

  public void addTimeBuckets(TimeBucket timeBucket) {
    this.timeBuckets.add(timeBucket);
  }

  public Map<String, String> getSummary() {
    return summary;
  }

  public void addSummary(String key, String value) {
    this.summary.put(key, value);
  }

  public List<Double> getCurrentValues() {
    return currentValues;
  }

  public void addCurrentValues(Double currentValue) {
    this.currentValues.add(currentValue);
  }

  public List<Double> getBaselineValues() {
    return baselineValues;
  }

  public void addBaselineValues(Double baselineValue) {
    this.baselineValues.add(baselineValue);
  }

  /**
   * Convert current instance into JSON String using ObjectMapper
   *
   * NOTE, as long as the getter and setter is implemented, the ObjectMapper constructs the JSON
   * String via
   * the getter and setter
   *
   * @return The JSON String of the condensed view of current instance
   */
  public String toJsonString() throws JsonProcessingException {
    // Convert the new AnomalyTimelinesView to condensed one
    return CondensedAnomalyTimelinesView.fromAnomalyTimelinesView(this).compress(DEFAULT_MAX_SIZE)
        .toJsonString();
  }

  /**
   * Given the JSON String of an AnomalyTimelinesView, return an instance of AnomalyTimelinesView
   *
   * NOTE, as long as the getter and setter is implemented, the ObjectMapper constructs the JSON
   * String via
   * the getter and setter
   *
   * @param jsonString The JSON String of an instance
   * @return An instance based on the given JSON String
   */
  public static AnomalyTimelinesView fromJsonString(String jsonString) throws IOException {
    AnomalyTimelinesView anomalyTimelinesView;
    try {
      // Try if the json string can be parsed to condensed view; otherwise, use AnomalyTimelinesView
      CondensedAnomalyTimelinesView condensedView = CondensedAnomalyTimelinesView
          .fromJsonString(jsonString);
      anomalyTimelinesView = condensedView.toAnomalyTimelinesView();
    } catch (Exception e) {
      LOG.warn(
          "The view instance is not in CondensedAnomalyTimelinesView; using the AnomalyTimelinesView instead");
      anomalyTimelinesView = OBJECT_MAPPER.readValue(jsonString, AnomalyTimelinesView.class);
    }
    return anomalyTimelinesView;
  }
}
