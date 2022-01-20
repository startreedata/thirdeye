package org.apache.pinot.thirdeye.restclient;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface InfoService {
  @GET
  Call<Map<String, Object>> getInfo(@Url String url);
}
