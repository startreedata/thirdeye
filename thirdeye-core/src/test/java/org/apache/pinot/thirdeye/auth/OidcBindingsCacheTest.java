package org.apache.pinot.thirdeye.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OidcBindingsCacheTest {

  private static final String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.NHVaYe26MbtOYhSKkoKYdFVomg4i8ZJd8_-RU8VNbftc4TSMb4bXP3l3YlNWACwyXPGffz5aXHc6lty1Y2t4SWRqGteragsVdZufDn5BlnJl9pdR_kdVFUsra2rWKEofkZeIC4yWytE58sMIihvo9H1ScmmVwBcQP6XETqYd0aSHp1gOa9RdUPDvoXQ5oqygTqVtxaDr6wUFKrKItgBMzWIdNZ6y7O9E0DhEPTbE9rfBo6KTFsHAZnMg4k68CDp2woYIaXbmYTWcvbzIuHO7_37GT79XdIwkm95QJ7hYC9RiwrV7mesbY4PAahERJawntho0my942XheVLmGwLMBkQ";

  private OidcBindingsCache cache;

  @BeforeClass
  public void init() throws Exception {
    OidcJWTProcessor processor = mock(OidcJWTProcessor.class);
    when(processor.process(any(SignedJWT.class), any(OidcContext.class))).thenReturn(new JWTClaimsSet.Builder().subject("test").build());
    cache = new OidcBindingsCache()
        .setProcessor(processor)
        .setContext(mock(OidcContext.class));
  }

  @Test
  public void cachedEntriesTest() throws Exception {
    ThirdEyePrincipal principal = cache.load(token);
    assertNotNull(principal);
    assertEquals(principal.getName(), "test");

  }

  @Test
  public void invalidJwtTokenTest() throws Exception {
    expectThrows(Exception.class, () -> cache.load("invalid-token"));
  }

}
