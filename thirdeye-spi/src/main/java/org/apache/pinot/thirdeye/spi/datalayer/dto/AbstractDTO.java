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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonInclude(Include.NON_NULL)
public abstract class AbstractDTO implements Serializable {

  private Long id;
  private int version;
  private Timestamp createTime;
  private String createdBy;
  private Timestamp updateTime;
  private String updatedBy;

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

  public Timestamp getCreateTime() {
    return createTime;
  }

  public AbstractDTO setCreateTime(final Timestamp createTime) {
    this.createTime = createTime;
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

  /**
   * All db models should be equated on their id if present.
   * Otherwise delegate to super.
   *
   * @param o other object
   * @return equality of 2 objects.
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AbstractDTO that = (AbstractDTO) o;
    if (id != null && that.id != null) {
      return Objects.equals(id, that.id);
    }
    return super.equals(o);
  }

  /**
   * Return the hashcode on id if possible. Otherwise delegate to super.
   *
   * @return hashcode
   */
  @Override
  public int hashCode() {
    if (id == null) {
      return super.hashCode();
    }
    return Objects.hash(id);
  }
}
