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
package ai.startree.thirdeye.spi.detection.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Components annotation
 * Components with this annotation will be registered and therefore can be configured from YAML
 * file.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Components {

  @JsonProperty String title() default "";

  @JsonProperty DetectionTag[] tags() default {};

  @JsonProperty String type() default "";

  @JsonProperty String description() default "";

  @JsonProperty boolean hidden() default false;

  @JsonProperty PresentationOption[] presentation() default {};

  @JsonProperty Param[] params() default {};
}

