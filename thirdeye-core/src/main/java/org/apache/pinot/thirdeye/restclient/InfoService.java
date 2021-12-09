package org.apache.pinot.thirdeye.restclient;

import java.util.HashMap;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface InfoService {
  @GET
  Call<HashMap<String, Object>> getInfo(@Url String url);
}
