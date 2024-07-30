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

import ai.startree.thirdeye.spi.Constants;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;

@Priority(Priorities.AUTHENTICATION)
public class BasicNamespacedCredentialAuthFilter<P extends Principal> extends AuthFilter<BasicNamespacedCredentials, P> {

    private BasicNamespacedCredentialAuthFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final BasicNamespacedCredentials credentials =
                getCredentials(
                    requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION),
                    requestContext.getHeaders().getFirst(Constants.NAMESPACE_HTTP_HEADER)
                    );
        if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
            throw unauthorizedHandler.buildException(prefix, realm);
        }
    }

    /**
     * Parses a Base64-encoded value of the `Authorization` header
     * in the form of `Basic dXNlcm5hbWU6cGFzc3dvcmQ=`.
     *
     * @param authHeader the value of the `Authorization` header
     * @param namespaceHeader the value of the `namespace` header
     * @return a username, password, namespace as {@link BasicNamespacedCredentials}
     */
    @Nullable
    private BasicNamespacedCredentials getCredentials(
        final String authHeader, 
        final String namespaceHeader) {
        if (authHeader == null) {
            return null;
        }

        final int space = authHeader.indexOf(' ');
        if (space <= 0) {
            return null;
        }

        final String method = authHeader.substring(0, space);
        if (!prefix.equalsIgnoreCase(method)) {
            return null;
        }

        final String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authHeader.substring(space + 1)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.warn("Error decoding credentials", e);
            return null;
        }

        // Decoded credentials is 'username:password'
        final int i = decoded.indexOf(':');
        if (i <= 0) {
            return null;
        }

        final String username = decoded.substring(0, i);
        final String password = decoded.substring(i + 1);
        return new BasicNamespacedCredentials(username, password, namespaceHeader);
    }

    /**
     * Builder for {@link BasicNamespacedCredentialAuthFilter}.
     * <p>An {@link Authenticator} must be provided during the building process.</p>
     *
     * @param <P> the principal
     */
    public static class Builder<P extends Principal> extends
            AuthFilterBuilder<BasicNamespacedCredentials, P, BasicNamespacedCredentialAuthFilter<P>> {

        @Override
        protected BasicNamespacedCredentialAuthFilter<P> newInstance() {
            return new BasicNamespacedCredentialAuthFilter<>();
        }
    }
}
