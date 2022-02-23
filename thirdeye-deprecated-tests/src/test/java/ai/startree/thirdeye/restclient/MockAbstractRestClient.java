/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.restclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class MockAbstractRestClient {

  public static Client setupMockClient(final Map<String, Object> expectedResponse) {
    Client client = mock(Client.class);

    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(anyString())).thenReturn(webTarget);

    Invocation.Builder builder = mock(Invocation.Builder.class);
    when(webTarget.request(anyString())).thenReturn(builder);
    when(builder.headers(any())).thenReturn(builder);

    Response response = mock(Response.class);
    when(builder.get()).thenReturn(response);
    when(response.readEntity(any(GenericType.class))).thenReturn(expectedResponse);

    return client;
  }
}
