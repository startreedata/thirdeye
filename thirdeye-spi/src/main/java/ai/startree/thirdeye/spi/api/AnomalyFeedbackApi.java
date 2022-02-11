/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AnomalyFeedbackApi {

  private Long id;
  private UserApi owner;
  private AnomalyFeedbackType type;
  private String comment;

  public Long getId() {
    return id;
  }

  public AnomalyFeedbackApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public UserApi getOwner() {
    return owner;
  }

  public AnomalyFeedbackApi setOwner(final UserApi owner) {
    this.owner = owner;
    return this;
  }

  public AnomalyFeedbackType getType() {
    return type;
  }

  public AnomalyFeedbackApi setType(final AnomalyFeedbackType type) {
    this.type = type;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public AnomalyFeedbackApi setComment(final String comment) {
    this.comment = comment;
    return this;
  }
}
