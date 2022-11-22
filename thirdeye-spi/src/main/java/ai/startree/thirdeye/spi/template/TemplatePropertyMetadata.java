/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.spi.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplatePropertyMetadata {

  private String name;
  private Object defaultValue;
  /**
   * Used to set a defaultValue to null. (defaultValue=null is interpreted as no defaultValue).
   */
  private boolean defaultIsNull = false;
  /**
   * If defaultValue is null and defaultIsNull is false, the UI can show this value as an example.
   */
  private Object exampleValue;

  /**
   * Helps the UI build input fields.
   */
  private Number minValue;
  private Number maxValue;
  /**
   * Should not be combined with min/maxValue
   */
  private List<Object> allowedValues;
  /**
   * Helps the UI build input fields.
   */
  private JsonType jsonType;

  public String getName() {
    return name;
  }

  public TemplatePropertyMetadata setName(final String name) {
    this.name = name;
    return this;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public TemplatePropertyMetadata setDefaultValue(final Object defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public Object getExampleValue() {
    return exampleValue;
  }

  public TemplatePropertyMetadata setExampleValue(final Object exampleValue) {
    this.exampleValue = exampleValue;
    return this;
  }

  public boolean isDefaultIsNull() {
    return defaultIsNull;
  }

  public TemplatePropertyMetadata setDefaultIsNull(final boolean defaultIsNull) {
    this.defaultIsNull = defaultIsNull;
    return this;
  }

  public Number getMinValue() {
    return minValue;
  }

  public TemplatePropertyMetadata setMinValue(final Number minValue) {
    this.minValue = minValue;
    return this;
  }

  public Number getMaxValue() {
    return maxValue;
  }

  public TemplatePropertyMetadata setMaxValue(final Number maxValue) {
    this.maxValue = maxValue;
    return this;
  }

  public List<Object> getAllowedValues() {
    return allowedValues;
  }

  public TemplatePropertyMetadata setAllowedValues(final List<Object> allowedValues) {
    this.allowedValues = allowedValues;
    return this;
  }

  public JsonType getJsonType() {
    return jsonType;
  }

  public TemplatePropertyMetadata setJsonType(final JsonType jsonType) {
    this.jsonType = jsonType;
    return this;
  }

  /**
   * See spec https://json-schema.org/understanding-json-schema/reference/type.html
   */
  public enum JsonType {
    STRING, NUMBER, INTEGER, OBJECT, ARRAY, BOOLEAN, NULL
  }
}
