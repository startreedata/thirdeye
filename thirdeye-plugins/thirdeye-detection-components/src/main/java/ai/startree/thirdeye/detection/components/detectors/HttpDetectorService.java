/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HttpDetectorService {

  @POST("http-detector")
  Call<HttpDetectorApi> evaluate(@Body HttpDetectorApi request);
}
