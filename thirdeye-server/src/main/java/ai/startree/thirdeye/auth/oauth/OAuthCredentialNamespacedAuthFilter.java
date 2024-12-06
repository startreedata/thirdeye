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
package ai.startree.thirdeye.auth.oauth;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator.AuthTokenAndNamespace;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import org.checkerframework.checker.nullness.qual.Nullable;

@Priority(Priorities.AUTHENTICATION)
public class OAuthCredentialNamespacedAuthFilter<P extends Principal> extends AuthFilter<AuthTokenAndNamespace, P> {

    /**
     * Query parameter used to pass Bearer token
     *
     * @see <a href="https://tools.ietf.org/html/rfc6750#section-2.3">The OAuth 2.0 Authorization Framework: Bearer Token Usage</a>
     */
    public static final String OAUTH_ACCESS_TOKEN_PARAM = "access_token";

    private OAuthCredentialNamespacedAuthFilter() {
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        String authToken = getCredentials(requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        // If Authorization header is not used, check query parameter where token can be passed as well
        if (authToken == null) {
            authToken = requestContext.getUriInfo().getQueryParameters().getFirst(OAUTH_ACCESS_TOKEN_PARAM);
        }
        final String namespace = requestContext.getHeaders().getFirst(Constants.NAMESPACE_HTTP_HEADER);

        if (!authenticate(requestContext, new AuthTokenAndNamespace(authToken, namespace), SecurityContext.BASIC_AUTH)) {
            throw unauthorizedHandler.buildException(prefix, realm);
        }
    }

    /**
     * Parses a value of the `Authorization` header in the form of `Bearer a892bf3e284da9bb40648ab10`.
     *
     * @param authHeader the value of the `Authorization` header
     * @return a token
     */
    private @Nullable String getCredentials(String authHeader) {
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

        return authHeader.substring(space + 1);
    }

    /**
     * Builder for {@link OAuthCredentialNamespacedAuthFilter}.
     * <p>An {@link Authenticator} must be provided during the building process.</p>
     *
     * @param <P> the type of the principal
     */
    public static class Builder<P extends Principal>
            extends AuthFilterBuilder<AuthTokenAndNamespace, P, OAuthCredentialNamespacedAuthFilter<P>> {

        @Override
        protected OAuthCredentialNamespacedAuthFilter<P> newInstance() {
            return new OAuthCredentialNamespacedAuthFilter<>();
        }
    }
}
