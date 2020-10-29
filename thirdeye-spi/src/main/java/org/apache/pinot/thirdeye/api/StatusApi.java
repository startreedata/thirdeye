package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.pinot.thirdeye.ThirdEyeStatus;

@JsonInclude(Include.NON_NULL)
public class StatusApi {

  private String code;
  private String msg;

  @SuppressWarnings("unused")
  public StatusApi() {
    // Used by Jackson deserialization
  }

  public StatusApi(ThirdEyeStatus status) {
    this.code = status.name();
    this.msg = status.getMessage();
  }

  public String getCode() {
    return code;
  }

  public StatusApi setCode(final String code) {
    this.code = code;
    return this;
  }

  public String getMsg() {
    return msg;
  }

  public StatusApi setMsg(final String msg) {
    this.msg = msg;
    return this;
  }
}
