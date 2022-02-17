/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
