/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.restclient;

import java.io.IOException;
import java.util.TreeMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAbstractRestClient extends AbstractRestClient {

  /**
   * Test compose url with null query parameters creates a valid url.
   */
  @Test
  public void testComposeUrlNullQueryParameters() throws IOException {
    String api = "/api/my/api";
    String host = "host";

    String actualUrl = composeUrl(host, api, null);
    String expectedUrl = String.format("http://%s%s", host, api);

    Assert.assertEquals(actualUrl, expectedUrl);
  }

  /**
   * Test compose url parameter with space create a valid url
   */
  @Test
  public void testComposeUrlGenericParameterWithSpaceAndSlash() throws IOException {
    String api = "/api/my/api";
    String host = "host";
    String parameterName = "parameter";
    String parameterValue = "param value";
    TreeMap<String, String> queryParameters = new TreeMap<String, String>();
    queryParameters.put(parameterName, parameterValue);

    String actualUrl = composeUrlGeneric(Protocol.HTTPS, host, api, queryParameters);
    String expectedUrl = "https://host/api/my/api?parameter=param%20value";

    Assert.assertEquals(actualUrl, expectedUrl);
  }
}
