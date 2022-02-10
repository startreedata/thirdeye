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

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;

@JsonInclude(Include.NON_NULL)
public class ApplicationApi implements ThirdEyeCrudApi<ApplicationApi> {

  private Long id;
  private String name;
  private Date created;
  private UserApi owner;

  public String getName() {
    return name;
  }

  public ApplicationApi setName(final String name) {
    this.name = name;
    return this;
  }

  public Long getId() {
    return id;
  }

  public ApplicationApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public ApplicationApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public UserApi getOwner() {
    return owner;
  }

  public ApplicationApi setOwner(final UserApi owner) {
    this.owner = owner;
    return this;
  }
}
