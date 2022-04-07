/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

public interface ThirdEyeCrudApi<T extends ThirdEyeCrudApi<T>> extends ThirdEyeApi {

  Long getId();

  T setId(Long id);
}
