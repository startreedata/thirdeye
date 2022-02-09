package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.ThirdEyeStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class StatusApi implements ThirdEyeApi {

  private ThirdEyeStatus code;
  private String msg;

  public ThirdEyeStatus getCode() {
    return code;
  }

  public StatusApi setCode(final ThirdEyeStatus code) {
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
