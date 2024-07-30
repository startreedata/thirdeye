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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A set of user-provided Basic Authentication credentials, consisting of a username and a
 * password.
 */
public class BasicNamespacedCredentials {

  private @NonNull
  final String username;
  private @NonNull
  final String password;
  private @Nullable
  final String namespace;

  /**
   * Creates a new BasicCredentials with the given username and password.
   *
   * @param username the username
   * @param password the password
   */
  public BasicNamespacedCredentials(final String username, final String password,
      final @Nullable String namespace) {
    this.username = requireNonNull(username);
    this.password = requireNonNull(password);
    this.namespace = namespace;
  }

  /**
   * Returns the credentials' username.
   *
   * @return the credentials' username
   */
  public @NonNull String getUsername() {
    return username;
  }

  /**
   * Returns the credentials' password.
   *
   * @return the credentials' password
   */
  public @NonNull String getPassword() {
    return password;
  }

  public @Nullable String getNamespace() {
    return namespace;
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password, namespace);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final BasicNamespacedCredentials other = (BasicNamespacedCredentials) obj;
    return Objects.equals(this.username, other.username)
        && Objects.equals(this.password, other.password)
        && Objects.equals(this.namespace, other.namespace)
        ;
  }

  @Override
  public String toString() {
    return "BasicCredentials{username=" + username + ", password=**********" + "namespace="
        + namespace + "}";
  }
}
