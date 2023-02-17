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
package ai.startree.thirdeye.spi.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class TemplatePropertyMetadata {

  /**
   * Exact name in the template
   */
  private String name;
  /**
   * Markdown description.
   */
  private String description;
  private Object defaultValue;
  /**
   * Used to set a defaultValue to null. (defaultValue=null is interpreted as no defaultValue).
   */
  private Boolean defaultIsNull;

  /**
   * Helps the UI build input fields.
   * If minValue is set and maxValue is null, this means there is no maxValue (in effect
   * Double.MAX_VALUE or Integer.MAX_VALUE) and vice versa.
   */
  private Number min;
  private Number max;
  /**
   * Should not be combined with min/maxValue
   */
  private List<Object> options;
  /**
   * Whether multiple options can be selected.
   */
  private Boolean isMultiselect = false;
  /**
   * Helps the UI build input fields.
   */
  private JsonType jsonType;

  /**
   * The logical step of the property when configuring an alert.
   */
  private Step step;

  /**
   * A free string subStep. Used to group properties belonging to the same step in smaller groups.
   */
  private String subStep;

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

  public Boolean isDefaultIsNull() {
    return defaultIsNull;
  }

  public TemplatePropertyMetadata setDefaultIsNull(final Boolean defaultIsNull) {
    this.defaultIsNull = defaultIsNull;
    return this;
  }

  public Number getMin() {
    return min;
  }

  public TemplatePropertyMetadata setMin(final Number min) {
    this.min = min;
    return this;
  }

  public Number getMax() {
    return max;
  }

  public TemplatePropertyMetadata setMax(final Number max) {
    this.max = max;
    return this;
  }

  public List<Object> getOptions() {
    return options;
  }

  public TemplatePropertyMetadata setOptions(final List<Object> options) {
    this.options = options;
    return this;
  }

  public JsonType getJsonType() {
    return jsonType;
  }

  public TemplatePropertyMetadata setJsonType(final JsonType jsonType) {
    this.jsonType = jsonType;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public TemplatePropertyMetadata setDescription(final String description) {
    this.description = description;
    return this;
  }

  public Boolean isMultiselect() {
    return isMultiselect;
  }

  public TemplatePropertyMetadata setMultiselect(final Boolean multiselect) {
    isMultiselect = multiselect;
    return this;
  }

  public Step getStep() {
    return step;
  }

  public TemplatePropertyMetadata setStep(
      final Step step) {
    this.step = step;
    return this;
  }

  public String getSubStep() {
    return subStep;
  }

  public TemplatePropertyMetadata setSubStep(final String subStep) {
    this.subStep = subStep;
    return this;
  }

  /**
   * See spec https://json-schema.org/understanding-json-schema/reference/type.html
   */
  public enum JsonType {
    STRING, NUMBER, INTEGER, OBJECT, ARRAY, BOOLEAN, NULL
  }

  public enum Step {
    DATA, PREPROCESS, DETECTION, FILTER, POSTPROCESS, RCA, OTHER
  }
}
