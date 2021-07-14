/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.spi.datalayer.dto;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFeedback;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFeedbackType;

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

  public void setFeedbackType(AnomalyFeedbackType feedbackType) {
    this.feedbackType = feedbackType;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
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
