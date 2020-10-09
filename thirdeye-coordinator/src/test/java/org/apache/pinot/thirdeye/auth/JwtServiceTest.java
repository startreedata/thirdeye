package org.apache.pinot.thirdeye.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class JwtServiceTest {

  private JwtService jwtService;

  @Before
  public void setUp() throws Exception {
    jwtService = new JwtService(
        new JwtConfiguration()
            .setIssuer("testIssuer")
            .setSigningKey("my-special-test-key")
    );
  }

  @Test
  public void testCreateAndValidate() {
    String accessToken;

    accessToken = jwtService.createAccessToken("principal");
    assertThat(jwtService.readPrincipal(accessToken))
        .isEqualTo(Optional.of("principal"));

    accessToken = jwtService.createAccessToken("principal234456");
    assertThat(jwtService.readPrincipal(accessToken))
        .isEqualTo(Optional.of("principal234456"));
  }
}
