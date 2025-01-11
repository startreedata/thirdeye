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
package ai.startree.thirdeye.auth.basic;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.dropwizard.auth.Authenticator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ThirdEyeBasicNamespacedAuthenticator implements
    Authenticator<BasicNamespacedCredentials, ThirdEyeServerPrincipal> {

  private final BasicAuthConfiguration configuration;

  private final Map<UsernamePassword, Set<String>> userPassToNamespace = new HashMap<>();

  private static final Set<String> SET_OF_NULL = Collections.singleton(null);
  private static final List<String> LIST_OF_NULL = Collections.singletonList(null);

  @Inject
  public ThirdEyeBasicNamespacedAuthenticator(
      @Nullable final BasicAuthConfiguration configuration) {
    this.configuration = configuration;
    if (configuration != null && configuration.getUsers() != null) {
      configuration.getUsers()
          .forEach(e -> userPassToNamespace.put(
              new UsernamePassword(e.getUsername(), e.getPassword()),
              e.getNamespaces() != null && !Objects.equals(e.getNamespaces(), LIST_OF_NULL) ?
                  // we can't use java.util.Set.copyOf. It returns a set that throws at s.contains(null)
                  ImmutableSet.copyOf(e.getNamespaces()) : SET_OF_NULL
          ));
    }
  }

  @Override
  public Optional<ThirdEyeServerPrincipal> authenticate(
      final BasicNamespacedCredentials basicNamespacedCredentials) {
    final UsernamePassword input = new UsernamePassword(
        basicNamespacedCredentials.getUsername(),
        basicNamespacedCredentials.getPassword());
    final Set<String> match = userPassToNamespace.get(input);
    if (match != null && match.contains(basicNamespacedCredentials.getNamespace())) {
      return Optional.of(
          new ThirdEyeServerPrincipal(basicNamespacedCredentials.getUsername(),
              "",
              AuthenticationType.BASIC_AUTH,
              basicNamespacedCredentials.getNamespace()));
    }
    return Optional.empty();
  }

  private record UsernamePassword(String username, String password) {}
}
