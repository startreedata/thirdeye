package ai.startree.thirdeye.spi.datalayer;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;

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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Templatable<?> that = (Templatable<?>) o;
    return Objects.equals(templatedValue, that.templatedValue) && Objects.equals(
        value,
        that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(templatedValue, value);
  }
}
