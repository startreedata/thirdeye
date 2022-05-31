/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface HttpDetectorService {

  @POST
  Call<HttpDetectorApi> evaluate(@Url String url, @Body HttpDetectorApi request);
}
