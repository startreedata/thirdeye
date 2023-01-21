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
package ai.startree.thirdeye.notification.anomalyfilter;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ychung on 2/14/17.
 *
 * This WeightThresholdAlertFilter checks if the weight level of given merged anomaly result is
 * between given up
 * and down threshold. The up and down threshold should be positive floating point number. This
 * class return false if
 * - downThreshold < weight < upThreshold; otherwise, return true.
 * Note that, down and up thresholds are supposed to be positive double value
 */
public class WeightThresholdAnomalyFilter extends BaseAnomalyFilter {

  private final static Logger LOG = LoggerFactory.getLogger(WeightThresholdAnomalyFilter.class);

  public static final String DEFAULT_UP_THRESHOLD = Double.toString(Double.POSITIVE_INFINITY);
  public static final String DEFAULT_DOWN_THRESHOLD = Double.toString(Double.POSITIVE_INFINITY);

  public static final String UP_THRESHOLD = "upThreshold";
  public static final String DOWN_THRESHOLD = "downThreshold";

  private double upThreshold = Double.parseDouble(DEFAULT_UP_THRESHOLD);
  private double downThreshold = Double.parseDouble(DEFAULT_DOWN_THRESHOLD);

  public double getUpThreshold() {
    return upThreshold;
  }

  public void setUpThreshold(double upThreshold) {
    this.upThreshold = Math.abs(upThreshold);
  }

  public double getDownThreshold() {
    return downThreshold;
  }

  public void setDownThreshold(double downThreshold) {
    this.downThreshold = Math.abs(downThreshold);
  }

  public WeightThresholdAnomalyFilter() {

  }

  public WeightThresholdAnomalyFilter(double upThreshold, double downThreshold) {
    setUpThreshold(upThreshold);
    setDownThreshold(downThreshold);
  }

  @Override
  public List<String> getPropertyNames() {
    return Collections
        .unmodifiableList(new ArrayList<>(Arrays.asList(UP_THRESHOLD, DOWN_THRESHOLD)));
  }

  // Check if the weight of the given MergedAnomalyResultDTO is greater or equal to the up threshold
  // or is less or equal to the down threshold.
  @Override
  public boolean isQualified(AnomalyDTO anomaly) {
    double weight = anomaly.getWeight();
    return (weight >= upThreshold) || (weight <= -1 * downThreshold);
  }
}
