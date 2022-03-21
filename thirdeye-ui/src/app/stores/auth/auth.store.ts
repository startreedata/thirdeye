import create from "zustand";
import { persist } from "zustand/middleware";
import { AuthStore } from "./auth.interfaces";

const LOCAL_STORAGE_KEY_AUTH = "LOCAL_STORAGE_KEY_AUTH";

// App store for authentication, persisted in browser local storage
export const useAuthStore = create<AuthStore>(
    persist(
        (set) => ({
            authDisabled: false,
            authenticated: false,
            accessToken: "",
            redirectPath: "",

            disableAuth: () => {
                set({
                    authDisabled: true,
                    authenticated: false,
                    accessToken: "",
                });
            },

            setAccessToken: (accessToken) => {
                set({
                    authDisabled: false,
                    authenticated: Boolean(accessToken),
                    accessToken: accessToken || "",
                });
            },

            clearAccessToken: () => {
                set({
                    authDisabled: false,
                    authenticated: false,
                    accessToken: "",
                });
            },

            setRedirectPath: (path: string) => {
                set({
                    redirectPath: path,
                });
            },

            clearRedirectPath: () => {
                set({
                    redirectPath: "",
                });
            },
        }),
        {
            name: LOCAL_STORAGE_KEY_AUTH, // Persist in browser local storage
        }
    )
);
