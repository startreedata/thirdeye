/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;

@JsonInclude(Include.NON_NULL)
public class UserApi implements ThirdEyeApi {

  private Long id;
  private String principal;
  private Date created;

  public Long getId() {
    return id;
  }

  public UserApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getPrincipal() {
    return principal;
  }

  public UserApi setPrincipal(final String principal) {
    this.principal = principal;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public UserApi setCreated(final Date created) {
    this.created = created;
    return this;
  }
}
