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
package ai.startree.thirdeye.spi.detection;

public interface AnomalyFeedback {

  /**
   * Set feedback type (e.g., anomaly, anomaly no action, etc.)
   *
   * @param feedbackType feedback type
   */
  AnomalyFeedback setFeedbackType(AnomalyFeedbackType feedbackType);

  /**
   * Get feedback type (e.g., anomaly, anomaly no action, etc.)
   *
   * @return feedback type
   */
  AnomalyFeedbackType getFeedbackType();

  /**
   * Set comment for this feedback.
   *
   * @param comment comment for this feedback.
   */
  AnomalyFeedback setComment(String comment);

  /**
   * Get comment of this feedback.
   *
   * @return comment of this feedback.
   */
  String getComment();
}
