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
package ai.startree.thirdeye.spi.detection;

import java.util.List;

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

  /**
   * Set reasons for this feedback.
   *
   * @param reasons a list of {@code AnomalyFeedbackReason}
   */
  AnomalyFeedback setReasons(List<AnomalyFeedbackReason> reasons);

  /**
   * Get reasons of this feedback. If no reason is associated with this feedback, an empty list is returned.
   *
   * @return reasons of this feedback.
   */
  List<AnomalyFeedbackReason> getReasons();
}
