/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
