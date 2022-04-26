/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.auth.AuthTestUtils.getJWK;
import static ai.startree.thirdeye.auth.AuthTestUtils.getToken;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OidcBindingsCacheTest {

  private OidcBindingsCache cache;

  @BeforeClass
  public void init() throws Exception {
    OidcJWTProcessor processor = mock(OidcJWTProcessor.class);
    when(processor.process(any(SignedJWT.class),
        any(OidcContext.class))).thenReturn(new JWTClaimsSet.Builder().claim("email","test").build());
    cache = new OidcBindingsCache()
        .setProcessor(processor)
        .setContext(mock(OidcContext.class));
  }

  @Test
  public void cachedEntriesTest() throws Exception {
    ThirdEyePrincipal principal = cache.load(getToken(getJWK(
        RandomStringUtils.randomAlphanumeric(16)),
        new JWTClaimsSet.Builder().build()));
    assertNotNull(principal);
    assertEquals(principal.getName(), "test");
  }

  @Test
  public void invalidJwtTokenTest() {
    expectThrows(Exception.class, () -> cache.load("invalid-token"));
  }
}
