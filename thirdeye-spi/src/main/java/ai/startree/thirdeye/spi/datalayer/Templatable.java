package ai.startree.thirdeye.spi.datalayer;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Templatable<T> {

  private String templatedValue;
  private T value;

  public String getTemplatedValue() {
    return templatedValue;
  }

  public Templatable<T> setTemplatedValue(final String templatedValue) {
    checkArgument(templatedValue.length() >= 4,
        "Invalid templated value variable string: %s. Expected format is ${VARIABLE_NAME}",
        templatedValue);
    this.templatedValue = templatedValue;
    return this;
  }

  public T getValue() {
    return value;
  }

  public Templatable<T> setValue(final T value) {
    this.value = value;
    return this;
  }
}
