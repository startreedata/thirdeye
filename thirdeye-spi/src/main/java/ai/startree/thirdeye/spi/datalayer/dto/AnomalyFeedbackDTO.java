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
package ai.startree.thirdeye.spi.datalayer.dto;

import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class AnomalyFeedbackDTO extends AbstractDTO implements AnomalyFeedback, Serializable {

  private static final long serialVersionUID = 1L;

  private AnomalyFeedbackType feedbackType;

  private String comment;

  public AnomalyFeedbackDTO() {
    this.setFeedbackType(AnomalyFeedbackType.NO_FEEDBACK);
    this.setComment("");
  }

  public AnomalyFeedbackDTO(AnomalyFeedback anomalyFeedback) {
    this();
    if (anomalyFeedback != null) {
      if (anomalyFeedback.getFeedbackType() != null) {
        this.setFeedbackType(anomalyFeedback.getFeedbackType());
      }
      if (StringUtils.isNotBlank(anomalyFeedback.getComment())) {
        this.setComment(anomalyFeedback.getComment());
      }
    }
  }

  public AnomalyFeedbackType getFeedbackType() {
    return feedbackType;
  }

  public AnomalyFeedbackDTO setFeedbackType(AnomalyFeedbackType feedbackType) {
    this.feedbackType = feedbackType;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public AnomalyFeedbackDTO setComment(String comment) {
    this.comment = comment;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AnomalyFeedbackDTO that = (AnomalyFeedbackDTO) o;
    return Objects.equals(getId(), that.getId()) && Objects
        .equals(feedbackType, that.getFeedbackType())
        && Objects.equals(comment, that.getComment());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), feedbackType, comment);
  }
}
