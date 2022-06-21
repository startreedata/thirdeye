/**
 * Copyright 2022 StarTree Inc
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
// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import axios from "axios";
import classNames from "classnames";
import { indexOf } from "lodash";
import React, {
    createContext,
    FunctionComponent,
    useContext,
    useEffect,
    useRef,
    useState,
} from "react";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetInfoV1 } from "../../rest/info/info.actions";
import { useGetOpenIDConfigurationV1 } from "../../rest/openid-configuration/openid-configuration.actions";
import { useAuthV1 } from "../../stores/auth-v1/auth-v1.store";
import { isBlockingAuthExceptionV1 } from "../../utils/auth/auth.util";
import {
    getFulfilledResponseInterceptorV1,
    getRejectedResponseInterceptorV1,
    getRequestInterceptorV1,
} from "../../utils/axios/axios.util";
import {
    clearHashParamV1,
    clearQueryParamV1,
    getHashParamV1,
    getQueryParamV1,
} from "../../utils/params/params.util";
import {
    addLeadingForwardSlashV1,
    removeTrailingForwardSlashV1,
} from "../../utils/url/url.util";
import { AppLoadingIndicatorV1 } from "../app-loading-indicator-v1/app-loading-indicator-v1.component";
import {
    AuthActionV1,
    AuthExceptionCodeV1,
    AuthHashParamV1,
    AuthProviderV1ContextProps,
    AuthProviderV1Props,
    AuthQueryParamV1,
    AuthRedirectMethodV1,
} from "./auth-provider-v1.interfaces";

export const AuthProviderV1: FunctionComponent<AuthProviderV1Props> = ({
    disableAuthOverride,
    clientId,
    redirectMethod,
    oidcIssuerAudiencePath,
    oidcIssuerLogoutPath,
    redirectPathBlacklist,
    appInitFailure,
    className,
    children,
    ...otherProps
}) => {
    const [authProviderLoading, setAuthProviderLoading] = useState(true);
    const [axiosLoading, setAxiosLoading] = useState(true);
    const [oidcIssuerUrl, setOidcIssuerUrl] = useState("");
    const [authorizationEndpoint, setAuthorizationEndpoint] = useState("");
    const [axiosRequestInterceptorId, setAxiosRequestInterceptorId] =
        useState(0);
    const [axiosResponseInterceptorId, setAxiosResponseInterceptorId] =
        useState(0);
    const loginFormRef = useRef<HTMLFormElement>(null);
    const logoutFormRef = useRef<HTMLFormElement>(null);
    const [
        authenticated,
        authDisabled,
        accessToken,
        authDisabledNotification,
        authExceptionCode,
        redirectHref,
        authAction,
        authActionData,
        enableAuth,
        disableAuth,
        setAccessToken,
        clearAuthDisabledNotification,
        clearAuth,
        clearAuthException,
        setRedirectHref,
        clearRedirectHref,
        setAuthAction,
        clearAuthAction,
        enableAuthDisabledNotification,
    ] = useAuthV1((state) => [
        state.authenticated,
        state.authDisabled,
        state.accessToken,
        state.authDisabledNotification,
        state.authExceptionCode,
        state.redirectHref,
        state.authAction,
        state.authActionData,
        state.enableAuth,
        state.disableAuth,
        state.setAccessToken,
        state.clearAuthDisabledNotification,
        state.clearAuth,
        state.clearAuthException,
        state.setRedirectHref,
        state.clearRedirectHref,
        state.setAuthAction,
        state.clearAuthAction,
        state.enableAuthDisabledNotification,
    ]);
    const {
        infoV1: fetchedInfoV1,
        getInfoV1,
        status: getInfoV1Status,
    } = useGetInfoV1();
    const {
        openIDConfigurationV1: fetchedOpenIDConfigurationV1,
        getOpenIDConfigurationV1,
        status: getOpenIDConfigurationV1Status,
    } = useGetOpenIDConfigurationV1();

    useEffect(() => {
        if (disableAuthOverride) {
            enableAuthDisabledNotification();
            disableAuth();
            setAuthProviderLoading(false);
        } else {
            // Loading for the first time
            initAuthProvider();
        }
    }, []);

    useEffect(() => {
        if (getInfoV1Status === ActionStatus.Done) {
            // Determine if auth is enabled or disabled
            initAuthStatus();

            return;
        }

        if (getInfoV1Status === ActionStatus.Error) {
            // Auth exception
            clearAuth(AuthExceptionCodeV1.InfoCallFailure);

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }
    }, [getInfoV1Status]);

    useEffect(() => {
        if (getOpenIDConfigurationV1Status === ActionStatus.Done) {
            // Initialize OpenID configuration
            initOpenIDConfiguration();

            return;
        }

        if (getOpenIDConfigurationV1Status === ActionStatus.Error) {
            // Auth exception
            clearAuth(AuthExceptionCodeV1.OpenIDConfigurationCallFailure);

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }
    }, [getOpenIDConfigurationV1Status]);

    useEffect(() => {
        // Access token changed, initialize axios
        setAxiosLoading(true);
        initAxios();
        setAxiosLoading(false);
    }, [accessToken]);

    useEffect(() => {
        // Auth action requested
        const requestedAction = authAction;
        const requestedActionData = authActionData;

        // Auth action has been used
        clearAuthAction();

        if (requestedAction === AuthActionV1.TriggerReload) {
            // Reload
            location.reload();

            return;
        }

        if (
            requestedAction === AuthActionV1.TriggerLocationRedirect &&
            requestedActionData
        ) {
            // Redirect to given location
            location.href = requestedActionData;

            return;
        }

        if (requestedAction === AuthActionV1.TriggerAuthLoginRedirect) {
            // Redirect to auth login
            loginFormRef &&
                loginFormRef.current &&
                loginFormRef.current.submit();

            return;
        }

        if (requestedAction === AuthActionV1.TriggerAuthLogoutRedirect) {
            // Redirect to auth logout
            logoutFormRef &&
                logoutFormRef.current &&
                logoutFormRef.current.submit();

            return;
        }
    }, [authAction, authActionData]);

    const initAuthProvider = (): void => {
        if (appInitFailure) {
            // App failed to initialize auth
            clearAuth(AuthExceptionCodeV1.AppInitFailure);

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }

        if (!clientId) {
            // Auth to be disabled
            disableAuth();

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }

        // Get auth query parameters, if available
        const loggedOut = getQueryParamV1(AuthQueryParamV1.LoggedOut);
        if (loggedOut) {
            // Redirecting to the app after auth logout
            clearAuth("");

            // Query parameter has been used
            clearQueryParamV1(AuthQueryParamV1.LoggedOut);
        }

        // Reset auth
        enableAuth();

        // Clear any blocking auth exceptions to try again
        if (
            isBlockingAuthExceptionV1(authExceptionCode as AuthExceptionCodeV1)
        ) {
            clearAuthException();
        }

        // Prepare to determine if auth is enabled or disabled
        getInfoV1();
    };

    const initAuthStatus = (): void => {
        if (fetchedInfoV1 && fetchedInfoV1.oidcIssuerUrl) {
            // Auth enabled, get OpenID configuration
            setOidcIssuerUrl(fetchedInfoV1.oidcIssuerUrl);

            // Determine if OpenID configuration is available or needs to be requested
            if (
                fetchedInfoV1.openidConfiguration &&
                fetchedInfoV1.openidConfiguration.authorization_endpoint
            ) {
                setAuthorizationEndpoint(
                    fetchedInfoV1.openidConfiguration.authorization_endpoint
                );

                // Begin auth
                initAuth();

                return;
            }

            getOpenIDConfigurationV1(fetchedInfoV1.oidcIssuerUrl);

            return;
        }

        if (fetchedInfoV1 && !fetchedInfoV1.oidcIssuerUrl) {
            // Auth disabled
            disableAuth();

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }

        if (!fetchedInfoV1) {
            // Auth exception
            clearAuth(AuthExceptionCodeV1.InfoMissing);

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }
    };

    const initOpenIDConfiguration = (): void => {
        if (
            fetchedOpenIDConfigurationV1 &&
            fetchedOpenIDConfigurationV1.authorization_endpoint
        ) {
            setAuthorizationEndpoint(
                fetchedOpenIDConfigurationV1.authorization_endpoint
            );

            // Begin auth
            initAuth();

            return;
        }

        if (
            !fetchedOpenIDConfigurationV1 ||
            !fetchedOpenIDConfigurationV1.authorization_endpoint
        ) {
            // Auth exception
            clearAuth(AuthExceptionCodeV1.OpenIDConfigurationMissing);

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }
    };

    const initAuth = (): void => {
        const urlAccessToken = getHashParamV1(AuthHashParamV1.AccessToken);
        if (urlAccessToken) {
            // Access token available in the URL
            setAccessToken(urlAccessToken);

            // Access token has been used
            clearHashParamV1(AuthHashParamV1.AccessToken);

            // Redirect to the app
            redirectToApp();

            return;
        }

        if (accessToken) {
            // Access token available, continue loading the app
            setAuthProviderLoading(false);

            return;
        }

        if (!accessToken) {
            // Access token not available, capture current location to redirect to after auth
            setRedirectHref(location.href);

            // Continue loading the app
            setAuthProviderLoading(false);

            return;
        }
    };

    const redirectToApp = (): void => {
        // Determine location to redirect to, if available
        const hrefToRedirect = redirectHref;

        // Redirect location, if available, has been used
        clearRedirectHref();

        let urlToRedirect;
        try {
            urlToRedirect = new URL(hrefToRedirect);
        } catch (error) {
            // No location to redirect to, continue loading the app
            setAuthProviderLoading(false);

            return;
        }

        if (
            urlToRedirect &&
            (urlToRedirect.pathname !== location.pathname ||
                urlToRedirect.search ||
                urlToRedirect.hash) &&
            indexOf(redirectPathBlacklist, urlToRedirect.pathname) === -1
        ) {
            // Location to redirect to is different from the current location or has additional
            // query/hash parameters and not in blacklist
            setAuthAction(
                AuthActionV1.TriggerLocationRedirect,
                urlToRedirect.href
            );

            return;
        }

        // No location to redirect to, continue loading the app
        setAuthProviderLoading(false);
    };

    const redirectToAuthLogin = (): void => {
        if (
            authenticated ||
            authDisabled ||
            isBlockingAuthExceptionV1(authExceptionCode as AuthExceptionCodeV1)
        ) {
            // App authenticated, auth disabled, or a blocking auth exception
            return;
        }

        if (!loginFormRef || !loginFormRef.current) {
            // Auth provider exception
            clearAuth(AuthExceptionCodeV1.InitFailure);

            return;
        }

        // Redirect to auth login
        setAuthAction(AuthActionV1.TriggerAuthLoginRedirect);
    };

    const initAxios = (): void => {
        // Clear existing interceptors
        axios.interceptors.request.eject(axiosRequestInterceptorId);
        axios.interceptors.response.eject(axiosResponseInterceptorId);

        // Set new interceptors
        setAxiosRequestInterceptorId(
            axios.interceptors.request.use(getRequestInterceptorV1(accessToken))
        );
        setAxiosResponseInterceptorId(
            axios.interceptors.response.use(
                getFulfilledResponseInterceptorV1(),
                getRejectedResponseInterceptorV1(handleUnauthenticatedAccess)
            )
        );
    };

    const handleUnauthenticatedAccess = (): void => {
        // Logout with exception
        handleLogout(AuthExceptionCodeV1.UnauthorizedAccess);
    };

    const handleLogout = (exceptionCode?: AuthExceptionCodeV1): void => {
        if (
            !authenticated ||
            authDisabled ||
            isBlockingAuthExceptionV1(authExceptionCode as AuthExceptionCodeV1)
        ) {
            // App not authenticated, auth disabled, or a blocking auth exception
            return;
        }

        // Determine if logout needs redirecting to auth logout
        if (
            oidcIssuerLogoutPath &&
            (!logoutFormRef || !logoutFormRef.current)
        ) {
            // Auth provider exception
            clearAuth(AuthExceptionCodeV1.InitFailure);

            return;
        }

        if (oidcIssuerLogoutPath) {
            // Redirect to auth logout
            setAuthAction(AuthActionV1.TriggerAuthLogoutRedirect);

            return;
        }

        // Logout
        clearAuth(exceptionCode || "");

        // Trigger reload
        setAuthAction(AuthActionV1.TriggerReload);
    };

    const authProviderV1Context = {
        authenticated: authenticated,
        authDisabled: authDisabled,
        accessToken: accessToken,
        authDisabledNotification: authDisabledNotification,
        authExceptionCode: authExceptionCode,
        login: redirectToAuthLogin,
        logout: handleLogout,
        clearAuthDisabledNotification: clearAuthDisabledNotification,
    };

    // Loading indicator
    if (authProviderLoading || axiosLoading) {
        return (
            <AppLoadingIndicatorV1
                {...otherProps}
                className={classNames(className, "auth-provider-v1")}
            />
        );
    }

    return (
        <AuthProviderV1Context.Provider value={authProviderV1Context}>
            {children}

            <div
                {...otherProps}
                className={classNames(className, "auth-provider-v1")}
            >
                {/* Login form */}
                <form
                    hidden
                    // When the user is automatically logged out, attaching automatic-logout=true
                    // query parameter displays appropriate message to the user (dex only)
                    action={`${
                        authorizationEndpoint && authorizationEndpoint
                    }?${AuthQueryParamV1.AutomaticLogout}=${
                        authExceptionCode ===
                        AuthExceptionCodeV1.UnauthorizedAccess
                    }`}
                    className="auth-provider-v1-login-form"
                    method={redirectMethod}
                    ref={loginFormRef}
                >
                    <input
                        readOnly
                        name="response_type"
                        // Distinction between dex and Auth0
                        // dex requires form to be submit via POST method and "token id_token"
                        // response type
                        // Auth0 requires form to be submit via GET method and "token" response type
                        value={
                            redirectMethod === AuthRedirectMethodV1.Post
                                ? "token id_token"
                                : "token"
                        }
                    />
                    <input readOnly name="client_id" value={clientId} />
                    <input
                        readOnly
                        name="redirect_uri"
                        value={location.origin}
                    />
                    <input readOnly name="scope" value="openid email profile" />
                    {oidcIssuerAudiencePath && (
                        <input
                            readOnly
                            name="audience"
                            // Form appropriate audience URL
                            value={`${removeTrailingForwardSlashV1(
                                oidcIssuerUrl
                            )}${addLeadingForwardSlashV1(
                                oidcIssuerAudiencePath
                            )}`}
                        />
                    )}
                    <input readOnly name="nonce" value="random_string" />
                    <input type="submit" value="" />
                </form>

                {/* Logout form */}
                {oidcIssuerLogoutPath && (
                    <form
                        hidden
                        // Form appropriate logout URL
                        action={`${removeTrailingForwardSlashV1(
                            oidcIssuerUrl
                        )}${addLeadingForwardSlashV1(oidcIssuerLogoutPath)}`}
                        className="auth-provider-v1-logout-form"
                        method={redirectMethod}
                        ref={logoutFormRef}
                    >
                        <input readOnly name="client_id" value={clientId} />
                        <input
                            readOnly
                            name="returnTo"
                            // After redirecting to auth logout, auth still needs to be cleared when
                            // redirecting back to the app
                            // Attaching ?logged-out=true signals that auth needs to be cleared when
                            // redirecting to the app next time
                            value={`${location.origin}?${AuthQueryParamV1.LoggedOut}=true`}
                        />
                        <input type="submit" value="" />
                    </form>
                )}
            </div>
        </AuthProviderV1Context.Provider>
    );
};

const AuthProviderV1Context = createContext<AuthProviderV1ContextProps>(
    {} as AuthProviderV1ContextProps
);

export const useAuthProviderV1 = (): AuthProviderV1ContextProps => {
    return useContext(AuthProviderV1Context);
};
