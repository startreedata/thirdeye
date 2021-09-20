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

package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFeedbackType;

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
