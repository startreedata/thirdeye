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

            disableAuth: (): void => {
                set({
                    authDisabled: true,
                    authenticated: false,
                    accessToken: "",
                });
            },

            setAccessToken: (accessToken: string): void => {
                set({
                    authDisabled: false,
                    authenticated: Boolean(accessToken),
                    accessToken: accessToken || "",
                });
            },

            clearAccessToken: (): void => {
                set({
                    authDisabled: false,
                    authenticated: false,
                    accessToken: "",
                });
            },
        }),
        {
            name: LOCAL_STORAGE_KEY_AUTH, // Persist in browser local storage
        }
    )
);
