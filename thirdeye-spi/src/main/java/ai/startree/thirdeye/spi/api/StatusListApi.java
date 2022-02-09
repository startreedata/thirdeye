package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class StatusListApi implements ThirdEyeApi {

  private List<StatusApi> list;

  public List<StatusApi> getList() {
    return list;
  }

  public StatusListApi setList(List<StatusApi> list) {
    this.list = list;
    return this;
  }
}
