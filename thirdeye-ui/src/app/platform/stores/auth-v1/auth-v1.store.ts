import create from "zustand";
import { persist } from "zustand/middleware";
import { AuthV1 } from "./auth-v1.interfaces";

const KEY_AUTH = "auth-v1";

// App store for auth, persisted in browser local storage
export const useAuthV1 = create<AuthV1>(
    persist(
        (set) => ({
            authenticated: false,
            authDisabled: false,
            authDisabledNotification: true,
            accessToken: "",
            authExceptionCode: "",
            redirectHref: "",
            authAction: "",
            authActionData: "",

            enableAuth: () => {
                set({
                    authDisabled: false,
                });
            },

            disableAuth: () => {
                set({
                    authenticated: false,
                    authDisabled: true,
                    accessToken: "",
                    authExceptionCode: "",
                });
            },

            enableAuthDisabledNotification: () => {
                set({
                    authDisabledNotification: true,
                });
            },

            clearAuthDisabledNotification: () => {
                set({
                    authDisabledNotification: false,
                });
            },

            setAccessToken: (token) => {
                set({
                    authenticated: Boolean(token),
                    authDisabled: false,
                    authDisabledNotification: true, // Auth disabled notification to be displayed next time auth is disabled
                    accessToken: token || "",
                    authExceptionCode: "",
                });
            },

            clearAuth: (exceptionCode) => {
                set({
                    authenticated: false,
                    authDisabled: false,
                    accessToken: "",
                    authExceptionCode: exceptionCode || "",
                });
            },

            clearAuthException: () => {
                set({
                    authExceptionCode: "",
                });
            },

            setRedirectHref: (href) => {
                set({
                    redirectHref: href,
                });
            },

            clearRedirectHref: () => {
                set({
                    redirectHref: "",
                });
            },

            setAuthAction: (action, actionData) => {
                set({
                    authAction: action || "",
                    authActionData: actionData || "",
                });
            },

            clearAuthAction: () => {
                set({
                    authAction: "",
                    authActionData: "",
                });
            },
        }),
        {
            name: KEY_AUTH, // Persist in browser local storage
            blacklist: ["authAction", "authActionData"], // Prevent persisting in state
        }
    )
);
