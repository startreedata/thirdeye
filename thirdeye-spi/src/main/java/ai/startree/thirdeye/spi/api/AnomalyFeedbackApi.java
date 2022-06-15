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
