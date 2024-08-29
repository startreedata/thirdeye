/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.auth;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.auth.basic.BasicAuthConfiguration;
import ai.startree.thirdeye.auth.basic.BasicNamespacedCredentials;
import ai.startree.thirdeye.auth.basic.ThirdEyeBasicNamespacedAuthenticator;
import ai.startree.thirdeye.auth.basic.UserCredentialConfiguration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.testng.annotations.Test;

public class BasicAuthenticatorTest {

  @Test
  public void basicAuthenticationTest() {
    final List<UserCredentialConfiguration> users = List.of(
        new UserCredentialConfiguration()
            .setUsername("user1")
            .setPassword("password1")
            .setNamespaces(Collections.singletonList(null)),
        // namespace not set explicitly should default to list of null
        new UserCredentialConfiguration()
            .setUsername("user2")
            .setPassword("password2"),
    new UserCredentialConfiguration()
        .setUsername("user3")
        .setPassword("password3")
        .setNamespaces(List.of("namespace1", "namespace2"))
    );
    final BasicAuthConfiguration configuration = new BasicAuthConfiguration().setUsers(users);
    final ThirdEyeBasicNamespacedAuthenticator authenticator = new ThirdEyeBasicNamespacedAuthenticator(
        configuration);

    //valid user
    final BasicNamespacedCredentials validUser =
        new BasicNamespacedCredentials("user1", "password1", null);
    final Optional<ThirdEyeServerPrincipal> authenticated = authenticator.authenticate(validUser);
    assertThat(authenticated).containsInstanceOf(ThirdEyeServerPrincipal.class);
    assertThat(authenticated.get().getName()).isEqualTo("user1");
    assertThat(authenticated.get().getNamespace()).isNull();

    //valid user but invalid namespace
    final BasicNamespacedCredentials validUserInvalidNamespace =
        new BasicNamespacedCredentials("user1", "password1", "invalid_namespace");
    assertThat(authenticator.authenticate(validUserInvalidNamespace)).isEmpty();

    //invalid user/password
    final BasicNamespacedCredentials invalidUserPass = 
        new BasicNamespacedCredentials("user1", "password2", null);
    assertThat(authenticator.authenticate(invalidUserPass)).isEmpty();

    //valid user/password with custom namespaces
    final BasicNamespacedCredentials validUserWithNamespaces =
        new BasicNamespacedCredentials("user3", "password3", "namespace1");
    final Optional<ThirdEyeServerPrincipal> authenticated1 = authenticator.authenticate(
        validUserWithNamespaces);
    assertThat(authenticated1).containsInstanceOf(
        ThirdEyeServerPrincipal.class);
    assertThat(authenticated1.get().getName()).isEqualTo("user3");
    assertThat(authenticated1.get().getNamespace()).isEqualTo("namespace1");

    //valid user/password with custom namespaces invalid namespace
    final BasicNamespacedCredentials validUserWithNamespacesInvalidNamespace =
        new BasicNamespacedCredentials("user3", "password3", null);
    assertThat(authenticator.authenticate(validUserWithNamespacesInvalidNamespace)).isEmpty();
  }
}
