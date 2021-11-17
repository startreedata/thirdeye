package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.AuthTestUtils.getJWK;
import static org.apache.pinot.thirdeye.auth.AuthTestUtils.getToken;
import static org.testng.Assert.expectThrows;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import java.util.Collections;
import java.util.Date;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OidcJWTProcessorTest {

  private static final String ISSUER = "http://identity.example.com";

  private OidcContext context;
  private JWK key;
  private OidcJWTProcessor processor;

  @BeforeClass
  public void init() throws Exception {
    context = new OidcContext(new OAuthConfig().setIssuer(ISSUER));
    key = getJWK(RandomStringUtils.randomAlphanumeric(16));
    processor = new OidcJWTProcessor(Collections.singletonList(key), context);
  }

  @Test
  public void processorSuccessTest() throws Exception {
    JWTClaimsSet expectedClaims = new JWTClaimsSet.Builder().subject("test")
        .issuer(ISSUER)
        .expirationTime(new Date(System.currentTimeMillis() + 36000000))
        .build();
    JWTClaimsSet actualClaims = processor.process(getToken(key, expectedClaims), context);
    assertNotNull(actualClaims);
    assertEquals(actualClaims.getIssuer(), expectedClaims.getIssuer());
    assertEquals(actualClaims.getSubject(), expectedClaims.getSubject());
  }

  @Test
  public void processorExpiredTokenTest() {
    JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("test")
        .issuer(ISSUER)
        .expirationTime(new Date(System.currentTimeMillis() - 36000000))
        .build();
    expectThrows(BadJWTException.class, () -> processor.process(getToken(key, claims), context));
  }

  @Test
  public void processorClaimsMismatchTest() {
    JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("test")
        .issuer(ISSUER.concat("suf"))
        .build();
    expectThrows(BadJWTException.class, () -> processor.process(getToken(key, claims), context));
  }

  @Test
  public void processorMissingClaimsTest() {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .issuer(ISSUER)
        .build();
    expectThrows(BadJWTException.class, () -> processor.process(getToken(key, claims), context));
  }
}
