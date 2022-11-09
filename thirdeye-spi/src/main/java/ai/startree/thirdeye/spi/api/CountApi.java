package ai.startree.thirdeye.spi.api;

public class CountApi {
  private Long count;
  private String message;

  public Long getCount() {
    return count;
  }

  public CountApi setCount(final Long count) {
    this.count = count;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public CountApi setMessage(final String message) {
    this.message = message;
    return this;
  }
}
