import create, { SetState } from "zustand";
import { persist } from "zustand/middleware";
import { AuthStore } from "./auth-store.interfaces";

const LOCAL_STORAGE_KEY_AUTH = "LOCAL_STORAGE_KEY_AUTH";

// Application store for authentication, persisted in browser local storage
export const useAuthStore = create<AuthStore>(
    persist<AuthStore>(
        (set: SetState<AuthStore>) => ({
            auth: false,
            accessToken: "",

            // Action for signing in
            setAccessToken: (accessToken: string): void => {
                set({
                    auth: true,
                    accessToken: accessToken,
                });
            },

            // Action for signing out
            removeAccessToken: (): void => {
                set({
                    auth: false,
                    accessToken: "",
                });
            },
        }),
        {
            name: LOCAL_STORAGE_KEY_AUTH, // Persist in browser local storage
        }
    )
);
