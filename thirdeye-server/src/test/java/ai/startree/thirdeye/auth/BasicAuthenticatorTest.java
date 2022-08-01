package ai.startree.thirdeye.auth;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.auth.basic.BasicAuthConfiguration;
import ai.startree.thirdeye.auth.basic.ThirdEyeBasicAuthenticator;
import ai.startree.thirdeye.auth.basic.UserCredentialConfiguration;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.List;
import org.testng.annotations.Test;

public class BasicAuthenticatorTest {

  @Test
  public void basicAuthenticationTest() throws AuthenticationException {
    final List<UserCredentialConfiguration> users = List.of(
        new UserCredentialConfiguration()
            .setUsername("user1")
            .setPassword("password1"),
        new UserCredentialConfiguration()
            .setUsername("user2")
            .setPassword("password2")
    );
    final BasicAuthConfiguration configuration = new BasicAuthConfiguration().setUsers(users);
    final ThirdEyeBasicAuthenticator authenticator = new ThirdEyeBasicAuthenticator(configuration);

    //valid user
    final BasicCredentials validUser = new BasicCredentials("user1", "password1");
    assertThat(authenticator.authenticate(validUser)).containsInstanceOf(ThirdEyePrincipal.class);
    assertThat(authenticator.authenticate(validUser).get().getName()).isEqualTo("user1");

    //invalid user
    final BasicCredentials invalidUser = new BasicCredentials("user1", "password2");
    assertThat(authenticator.authenticate(invalidUser)).isEmpty();
  }
}
