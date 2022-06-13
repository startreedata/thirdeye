/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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

