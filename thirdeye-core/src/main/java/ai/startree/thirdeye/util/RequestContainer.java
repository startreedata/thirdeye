/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;

/**
 * Wrapper for ThirdEye request with derived metric expressions
 */
public class RequestContainer {

  final ThirdEyeRequest request;

  RequestContainer(ThirdEyeRequest request) {
    this.request = request;
  }

  public ThirdEyeRequest getRequest() {
    return request;
  }
}
