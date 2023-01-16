/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.spi.datalayer;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonInclude(Include.NON_NULL)
public class Templatable<T> {

  private static final Logger LOG = LoggerFactory.getLogger(Templatable.class);

  private @Nullable String templatedValue;
  private @Nullable T value;
  /**
   * Name of the field containing the value. Used by TemplateEngineTemplatableSerializer.
   */
  public static final String VALUE_FIELD_STRING = "value";

  public Templatable() {}

  public static <T> Templatable<T> of(final T value) {
    final Templatable<T> templatable = new Templatable<>();
    templatable.setValue(value);
    return templatable;
  }

  @JsonProperty("templatedValue")
  // getter is shortened for ease of use and readability
  public @Nullable String templatedValue() {
    return templatedValue;
  }

  public Templatable<T> setTemplatedValue(final @NonNull String templatedValue) {
    // fixme cyril perform a better check
    checkArgument(templatedValue.length() >= 4,
        "Invalid templated value variable string: %s. Expected format is ${VARIABLE_NAME}",
        templatedValue);
    this.templatedValue = templatedValue;
    return this;
  }

  @JsonProperty("value")
  // getter is shortened for ease of use and readability
  public @Nullable T value() {
    return value;
  }

  public Templatable<T> setValue(@Nullable final T value) {
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

  @Override
  public String toString() {
    LOG.error("Calling toString on a Templatable. Most likely caused by an incorrect implementation targeting the wrapped value or a debug mode.");
    return "Templatable{" +
        "templatedValue='" + templatedValue + '\'' +
        ", value=" + value +
        '}';
  }
}
