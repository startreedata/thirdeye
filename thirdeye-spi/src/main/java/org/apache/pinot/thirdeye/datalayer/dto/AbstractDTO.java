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

package org.apache.pinot.thirdeye.datalayer.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class AbstractDTO implements Serializable {

  private Long id;
  private int version;
  protected Timestamp updateTime;
  protected String createdBy;
  protected String updatedBy;

  public Long getId() {
    return id;
  }

  public AbstractDTO setId(final Long id) {
    this.id = id;
    return this;
  }

  public int getVersion() {
    return version;
  }

  public AbstractDTO setVersion(final int version) {
    this.version = version;
    return this;
  }

  public Timestamp getUpdateTime() {
    return updateTime;
  }

  public AbstractDTO setUpdateTime(final Timestamp updateTime) {
    this.updateTime = updateTime;
    return this;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public AbstractDTO setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public AbstractDTO setUpdatedBy(final String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AbstractDTO that = (AbstractDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
